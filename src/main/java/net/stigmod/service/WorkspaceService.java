/*
 * Copyright 2014-2016, Stigmergic-Modeling Project
 * SEIDR, Peking University
 * All rights reserved
 *
 * Stigmergic-Modeling is used for collaborative groups to create a conceptual model.
 * It is based on UML 2.0 class diagram specifications and stigmergy theory.
 */

package net.stigmod.service;

import com.google.gson.Gson;
import javafx.util.Pair;
import net.stigmod.domain.conceptualmodel.*;
import net.stigmod.domain.info.CcmDetail;
import net.stigmod.domain.info.IcmDetail;
import net.stigmod.domain.info.ModelingOperationLog;
import net.stigmod.domain.info.ModelingResponse;
import net.stigmod.domain.system.CollectiveConceptualModel;
import net.stigmod.domain.system.IndividualConceptualModel;
import net.stigmod.domain.system.ModelingOperations;
import net.stigmod.repository.node.*;
import net.stigmod.repository.relationship.*;
import net.stigmod.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author Shijun Wang
 * @version 2016/2/27
 */
@Service
public class WorkspaceService {

    @Autowired
    private Neo4jOperations neo4jTemplate;

    @Autowired
    private ClassNodeRepository classNodeRepository;

    @Autowired
    private RelationNodeRepository relationNodeRepository;

    @Autowired
    private ValueNodeRepository valueNodeRepository;

    @Autowired
    private VertexRepository vertexRepository;

    @Autowired
    private EdgeRepository edgeRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CollectiveConceptualModelRepository ccmRepository;

    @Autowired
    private IndividualConceptualModelRepository icmRepository;

    @Autowired
    private ModelingOperationsRepository modOpsRepository;

    static private Long dbSavingDelay = 200L;  // 200ms 延迟


    /**
     * 从前端向后端同步建模结果
     * （之所以叫同步，是因为一来是后端获取前端的建模结果并保存，二来后端也向前端传递 frontId 到 backId 的映射）
     * @param molJsonString 字符串形式的 LOG
     * @return 要返回给前端的 Response
     */
    public ModelingResponse syncModelingOperations(String molJsonString) {
        System.out.println(molJsonString);
        ModelingOperationLog mol = this.constructMOL(molJsonString);
        if (mol.log.size() <= 1) {  // log 长度小于等于 1 说明没有前端操作需要执行或保存（其中一条日志是 UPD NUM）
            return new ModelingResponse();
        }
        this.storeModOps(mol);
        return this.executeMOL(mol);
    }

    /**
     * 构造用于前端显示的 ICM 详细数据
     * @param icmId ICM ID
     * @return IcmDetail
     */
    public IcmDetail getIcmDetail(Long icmId) {
        IcmDetail icmDetail = new IcmDetail();

        // 获取所有的类节点
        List<Map<String, Object>> classNamesAndIds = classNodeRepository.getAllClassNamesAndIdsByIcmId(icmId);
        icmDetail.addClasses(classNamesAndIds);

        // 获取所有的关系节点
        List<Map<String, Object>> relationshipsAndTypes = relationNodeRepository.getAllRelationshipsAndTypesByIcmId(icmId);

        for (Map<String, Object> relationshipAndType : relationshipsAndTypes) {
            Long relationshipId = ((Number) relationshipAndType.get("relationshipId")).longValue();
            String relationshipType = (String) relationshipAndType.get("relationshipType");

            List<Map<String, Object>> classEnds = relationNodeRepository.getClassEndRelationshipPropertiesByIcmIdAndRelationshipId(icmId, relationshipId);
            List<Map<String, Object>> relationshipProperties = relationNodeRepository.getAllRelationshipPropertiesByIcmIdAndRelationshipId(icmId, relationshipId);

            boolean isNoTypeAttribute = classEnds.size() < 2;  // 若 classEnds 只有一边有，则说明这是一个没有 type property 的 attribute
            String className0;
            String className1 = null;

            if (isNoTypeAttribute) {
                try {
                    className0 = (String) classEnds.get(0).get("propertyValue");
                } catch (IndexOutOfBoundsException ex) {
                    System.out.println("!! icmId: " + icmId.toString() + ", relationshipId: " + relationshipId.toString());
                    throw ex;
                }
            } else {
                boolean isTheFirstElementEnd0 = classEnds.get(0).get("port").equals("E0");
                className0 = isTheFirstElementEnd0
                        ? (String) classEnds.get(0).get("propertyValue")
                        : (String) classEnds.get(1).get("propertyValue");
                className1 = isTheFirstElementEnd0
                        ? (String) classEnds.get(1).get("propertyValue")
                        : (String) classEnds.get(0).get("propertyValue");
            }

            if (relationshipType.equals("isAttribute")) {  // 类的属性

                String attributeName = null;
                Map<String, String> propertyAndValues = new HashMap<>();

                if (!isNoTypeAttribute) {  // attribute 有 type property
                    String attributeType = className1;
                    if (attributeType.startsWith("_")) {
                        attributeType = attributeType.substring(1);  // 若为内置类型，则去掉开头的下划线
                    }
                    propertyAndValues.put("type", attributeType);
                }

                for (Map<String, Object> relationshipProperty : relationshipProperties) {
                    if (relationshipProperty.get("port").equals("E1")) {  // E1 端是有有效信息的端
                        String propertyName = (String) relationshipProperty.get("propertyName");
                        if (propertyName.equals("role")) {
                            attributeName = (String) relationshipProperty.get("propertyValue");
                        }
                        String key = propertyName.equals("role") ? "name" : propertyName;
                        String value = (String) relationshipProperty.get("propertyValue");
                        if (value.equals("#true")) {  // 对于 true 和 false 特殊处理
                            value = "True";
                        } else if (value.equals("#false")) {  // 对于 true 和 false 特殊处理
                            value = "False";
                        }
                        propertyAndValues.put(key, value);
                    }
                }

                // 添加类的属性
                icmDetail.addAttribute(className0, attributeName, relationshipId, propertyAndValues);

            } else {  // 类间的关系

                relationshipType = relationshipType.substring(2);  // 去掉开头的“is”，如 “isGeneralization” -> “Generalization”
                String relationshipName = "";
                Map<String, List<String>> propertyAndValues = new HashMap<>();

                for (Map<String, Object> relationshipProperty : relationshipProperties) {
                    String port = (String) relationshipProperty.get("port");
                    String propertyName = (String) relationshipProperty.get("propertyName");
                    String propertyValue = (String) relationshipProperty.get("propertyValue");
                    if (propertyValue.equals("#true")) {  // 对于 true 和 false 特殊处理
                        propertyValue = "True";
                    } else if (propertyValue.equals("#false")) {  // 对于 true 和 false 特殊处理
                        propertyValue = "False";
                    }

                    if (propertyName.equals("name")) {  // 有可能没有名字
                        relationshipName = propertyValue;
                        continue;
                    }
                    switch (port) {  // 确保 E0 在 List 的第一个元素， E1 是第二个
                        case "E0":
                            if (propertyAndValues.containsKey(propertyName)) {
                                propertyAndValues.get(propertyName).set(0, propertyValue);
                            } else {
                                propertyAndValues.put(propertyName, new ArrayList<>(Arrays.asList(propertyValue, "")));
                            }
                            break;
                        case "E1":
                            if (propertyAndValues.containsKey(propertyName)) {
                                propertyAndValues.get(propertyName).set(1, propertyValue);
                            } else {
                                propertyAndValues.put(propertyName, new ArrayList<>(Arrays.asList("", propertyValue)));
                            }
                            break;
                        default:
                            // DO NOTHING （port 为空的情况，什么都不做。port 为空时，意味着是 name 边或 type 边）
                            break;
                    }
                }
                propertyAndValues.put("type", new ArrayList<>(Arrays.asList(relationshipType, relationshipName)));
                propertyAndValues.put("class", new ArrayList<>(Arrays.asList(className0, className1)));

                // 添加关系
                icmDetail.addRelationship(relationshipId, propertyAndValues);
            }
        }

        // 添加 Orders
        List<Order> attributeOrders = orderRepository.getByIcmIdAndType(icmId, "AttOdr");
        icmDetail.addAttributeOrders(attributeOrders);
        List<Order> relationshipOrders = orderRepository.getByIcmIdAndType(icmId, "RelOdr");
        icmDetail.addRelationshipOrders(relationshipOrders);

        return icmDetail;
    }

    /**
     * 构造用于前端推荐的 CCM 详细信息
     * @param ccmId ccmId
     * @return CCM 详细信息
     */
    public CcmDetail getCcmDetail(Long ccmId) {

        CcmDetail ccmDetail = new CcmDetail();
        Map<Long, String> builtInClassIdNameMapping = new HashMap<>();  // 用于在关系构造时识别出关系两端类是否是内置的

        // 向 ccmDetail 中添加所有类信息
        List<Map<String, Object>> classInfos = classNodeRepository.getAllClassInfoBycmId(ccmId);
        for (Map<String, Object> classInfo : classInfos) {
            Long classId = ((Integer) classInfo.get("classId")).longValue();
            Long classRef = ((Integer) classInfo.get("classRef")).longValue();
            String className = (String) classInfo.get("className");
            Long nameRef = ((Integer) classInfo.get("nameRef")).longValue();

            if (!className.startsWith("_")) {  // 过滤掉内置类
                ccmDetail.addClass(classId, classRef, className, nameRef);
            } else {
                builtInClassIdNameMapping.put(classId, className);
            }
        }

        // 获取所有关系节点，向 ccmDetail 中添加所有属性和关系
        List<RelationNode> relationNodesTemp = (List<RelationNode>)(List) vertexRepository.getAllByCcmIdAndLabel(ccmId, "Relationship");  // 获取所有关系节点
        for (RelationNode relationNodeTemp : relationNodesTemp) {

            // 深度 1 足够取到所有的关系节点、与关系直接相连的值节点、类节点，至于类名值节点，则从已经构建好的 ccmDetail 中获取
            Long relationshipId = relationNodeTemp.getId();
            Long relationshipRef = (long) relationNodeTemp.getIcmSet().size();
            RelationNode relationNode = relationNodeRepository.findOne(relationshipId, 1);

            // 遍历该 relationship 到 class 的边
            for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
                if (r2cEdge.getIcmSet().size() > 0) {  // 滤掉没有用户引用的边
                    Long classId = r2cEdge.getEnder().getId();
                    String classIdString = builtInClassIdNameMapping.containsKey(classId)  // 将内置类的 Id 换成名字
                            ? builtInClassIdNameMapping.get(classId)
                            : classId.toString();
                    ccmDetail.addRelationshipProperty(relationshipId, r2cEdge.getPort(), r2cEdge.getName(),
                            classIdString, relationshipRef, (long) r2cEdge.getIcmSet().size());
                }
            }

            // 遍历该 relationship 到 value 的边
            for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
                if (r2vEdge.getIcmSet().size() > 0) {  // 滤掉没有用户引用的边
                    String propertyName;
                    String propertyValue;
                    if (r2vEdge.getPort().equals("") && r2vEdge.getName().startsWith("is")) {  // 特殊处理 type 边
                        propertyName = "type";
                        propertyValue = r2vEdge.getName().substring(2);  // 去掉开头的 is
                    } else {
                        propertyName = r2vEdge.getName();
                        propertyValue = r2vEdge.getEnder().getName();
                        if (propertyValue.equals("#true")) {  // 去掉 #true #false 的 # 并大写第一个字母
                            propertyValue = "True";
                        }
                        if (propertyValue.equals("#false")) {  // 去掉 #true #false 的 # 并大写第一个字母
                            propertyValue = "False";
                        }
                    }

                    ccmDetail.addRelationshipProperty(relationshipId, r2vEdge.getPort(), propertyName,
                            propertyValue, relationshipRef, (long) r2vEdge.getIcmSet().size());
                }
            }
        }

        // relationshipId 按类型填入 list
        ccmDetail.fillInAttAndRelLists();

        return ccmDetail;
    }

    /**
     * 将 JSON 字符串转换为 ModelingOperationLog 对象
     * @param molJsonString 字符串形式的 LOG
     * @return 对象形式的 LOG
     */
    private ModelingOperationLog constructMOL(String molJsonString) {
        Gson gson = new Gson();
        return gson.fromJson(molJsonString, ModelingOperationLog.class);
    }

    /**
     * 执行建模操作日志
     * @param mol 前端传回来的操作日志
     */
    @Transactional
    private ModelingResponse executeMOL(ModelingOperationLog mol) {
        List<List<String>> ops = mol.log;
        ModelingOperationLog.OrderChanges orderChanges = mol.orderChanges;
        Long ccmId = mol.ccmId;
        Long icmId = mol.icmId;
        ModelingResponse modelingResponse = new ModelingResponse();

        // 由于 icm 与 user 点相连，而 user 点在内存中，所以这个 icm 虽然
        // 是 findOne() 无第二参数取出的，但其中中是有到 user 和 ccm 的连
        // 接的 (后来为了语义清晰，显式加上了 findOne() 的第二参数 1)
        IndividualConceptualModel icm = icmRepository.findOne(icmId, 1);

        // 操作序列
        List<List<String>> bigOp = new ArrayList<>();  // 用于构造像“添加关系、添加属性”这样的大操作
        boolean isConstructingBigOp = false;
        String bigOpObjId = "";
        for (List<String> op : ops) {

            if (op.get(1).equals("ADD") && (op.get(2).equals("RLT") || op.get(2).equals("ATT")) ) {  // 添加关系或属性，开启大操作构造
                if (isConstructingBigOp) {  // 上一条操作属于上一个大操作
                    executeBIGOP(bigOp, ccmId, icmId, modelingResponse, icm);  // 执行构造好的大操作
                    bigOp = new ArrayList<>();
                } else {  // 上一条操作是一般小操作
                    isConstructingBigOp = true;
                }
                bigOp.add(op);
                bigOpObjId = op.get(4);  // 用于准确找到隶属于此大操作的小操作

            } else if (isConstructingBigOp) {
                if (op.get(1).equals("ADD")
                        && (op.get(2).equals("POR") || op.get(2).equals("POA"))
                        && bigOpObjId.equals(op.get(4))) {  // 添加 ADD POR/POA 操作
                    bigOp.add(op);
                } else if (op.get(1).equals("ODI")
                        && (op.get(2).equals("RLT") || op.get(2).equals("ATT"))
                        && bigOpObjId.equals(op.get(4))) {  // 忽略“添加顺序”操作
                    // DO NOTHING
                } else {  // 结束大操作的构造，开始执行
                    executeBIGOP(bigOp, ccmId, icmId, modelingResponse, icm);  // 执行构造好的大操作
                    executeOP(op, ccmId, icmId, modelingResponse, icm);  // 执行本条操作
                    isConstructingBigOp = false;
                    bigOp = new ArrayList<>();
                }

            } else {  // 一般的小操作
                executeOP(op, ccmId, icmId, modelingResponse, icm);
            }

        }

        // 顺序改变 (attribute)
        for (Map.Entry<String, List<String>> attributeOrderChanges : orderChanges.classes.entrySet()) {
            String name = attributeOrderChanges.getKey();
            List<String> orderList = attributeOrderChanges.getValue();
            Order order = this.getOrder(icmId, "AttOdr", name);
            order.setOrderList(orderList);
            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
            orderRepository.save(order);
        }

        // 顺序改变 (relationship)
        for (Map.Entry<String, List<String>> attributeOrderChanges : orderChanges.relationGroups.entrySet()) {
            String name = attributeOrderChanges.getKey();
            List<String> orderList = this.getBackIdsFromFrontIds(icmId, attributeOrderChanges.getValue());  // 将可能的 frontId 转换为 backId
            Order order = this.getOrder(icmId, "RelOdr", name);
            order.setOrderList(orderList);
            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
            orderRepository.save(order);
        }

        return modelingResponse;
    }

    /**
     * 存储建模序列
     * @param mol 建模日志
     */
    private void storeModOps(ModelingOperationLog mol) {
        ModelingOperations modOps = modOpsRepository.getModOpsByIcmId(mol.icmId);
        modOps.addOperations(mol.log);
        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
        modOpsRepository.save(modOps);
    }

    /**
     * 执行大操作
     * @param bigOp bigOp
     * @param ccmId ccmId
     * @param icmId icmId
     * @param modelingResponse modelingResponse
     * @param icm icm
     */
    @Transactional
    private void executeBIGOP(List<List<String>> bigOp, Long ccmId, Long icmId, ModelingResponse modelingResponse, IndividualConceptualModel icm) {
        List<String> op = bigOp.get(0);
        String opV = op.get(1);  // 谓语
        String opO = op.get(2);  // 宾语

        neo4jTemplate.clear();  // 操作前清空 session 中的 mappingContext，及其重要

        if (opV.equals("ADD") && opO.equals("RLT")) {

            // ADD RLT relationGroup relation relationCCMId addingType (fresh, binding)
            String relationGroupName = op.get(3);
            String relationshipId = op.get(4);  // 同 op.get(5)

            String addingType = op.get(6);
            boolean isFreshCreation = addingType.equals("fresh");

            // 获取 relationship node
            RelationNode relationNode;
            Map<String, ValueNode> valueNodePool = new HashMap<>();  // 用于缓存尚未存入数据库的值节点，避免重复创建值节点

            if (isFreshCreation) {  // 全新创建
                relationNode = new RelationNode(ccmId, icmId);

                for (List<String> smallOp : bigOp) {

                    System.out.print("## Operation: ( ccmId: " + ccmId.toString() + ", icmId: " + icmId.toString() + " ) [ADD RLT FRESH] ");
                    for (String opElem : smallOp) {  // 打印
                        System.out.print(opElem);
                        System.out.print(" ");
                    }
                    System.out.print("\n");

                    if (smallOp.get(2).equals("RLT")) continue;  // 跳过 ADD RLT

                    // ADD POR relationGroup relation property value
                    String propertyName = smallOp.get(5);
                    String propertyValueE0 = smallOp.get(6);
                    String propertyValueE1 = smallOp.get(7);

                    switch (propertyName) {
                        case "type": {  // 关系的类型和名称（名称可能为空字符串）

                            // 添加关系类型
                            String typeEdgeName = "is" + propertyValueE0;
                            this.addR2VEdgeWithoutSaving(ccmId, icmId, "", typeEdgeName, relationNode, "#true", valueNodePool);

                            // 若有关系名字，则添加
                            if (!propertyValueE1.equals("")) {
                                this.addR2VEdgeWithoutSaving(ccmId, icmId, "", "name", relationNode, propertyValueE1, valueNodePool);
                            }

                            break;
                        }
                        case "class": {  // 关系两端的类

                            // 获取关系两端的 class node (必定存在于 ICM 中)
                            ClassNode classNodeE0 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE0);
                            ClassNode classNodeE1 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE1);
                            assert classNodeE0 != null && classNodeE1 != null;

                            // 添加 r2cEdge
                            this.addR2CEdgeWithoutSaving(ccmId, icmId, "E0", "class", relationNode, classNodeE0);
                            this.addR2CEdgeWithoutSaving(ccmId, icmId, "E1", "class", relationNode, classNodeE1);

                            break;
                        }
                        default: {  // 一般 property

                            this.addR2VEdgeWithoutSaving(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);
                            this.addR2VEdgeWithoutSaving(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);

                            break;
                        }
                    }
                    modelingResponse.addMessage("Add property [" + propertyName + "] to relationship [" + relationshipId.toString() + "] of relationship group [" + relationGroupName + "] successfully.");
                }

            } else {  // 绑定创建
                relationNode = relationNodeRepository.findOne(Long.parseLong(relationshipId, 10), 2);
                assert relationNode != null;
                this.solveASDNBug(relationNode);
                relationNode.addIcmId(icmId);
                relationNode.setIsSettled(false);  // 有待融合算法进一步处理

                for (List<String> smallOp : bigOp) {

                    System.out.print("## Operation: ( ccmId: " + ccmId.toString() + ", icmId: " + icmId.toString() + " ) [ADD RLT BINDING] ");
                    for (String opElem : smallOp) {  // 打印
                        System.out.print(opElem);
                        System.out.print(" ");
                    }
                    System.out.print("\n");

                    if (smallOp.get(2).equals("RLT")) continue;  // 跳过 ADD RLT

                    // ADD POR relationGroup relation property value
                    String propertyName = smallOp.get(5);
                    String propertyValueE0 = smallOp.get(6);
                    String propertyValueE1 = smallOp.get(7);

                    switch (propertyName) {
                        case "type": {  // 关系的类型和名称（名称可能为空字符串）

                            // 添加关系类型
                            String typeEdgeName = "is" + propertyValueE0;
                            this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "", typeEdgeName, relationNode, "#true", valueNodePool);

                            // 若有关系名字，则添加
                            if (!propertyValueE1.equals("")) {
                                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "", "name", relationNode, propertyValueE1, valueNodePool);
                            }

                            break;
                        }
                        case "class": {  // 关系两端的类 (必定存在于 ICM 中)

                            // 获取关系两端的 class node (必定存在于 ICM 中)
                            ClassNode classNodeE0 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE0);
                            ClassNode classNodeE1 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE1);
                            assert classNodeE0 != null && classNodeE1 != null;

                            // 添加关系到类型类的边
                            this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", "class", relationNode, classNodeE0);
                            this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", "class", relationNode, classNodeE1);

                            break;
                        }
                        default: {  // 一般 property

                            // 添加 (relationship)-[e0.{propertyName}]->(value)
                            this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);

                            // 添加 (relationship)-[e1.{propertyName}]->(value)
                            this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);

                            break;
                        }
                    }
                    modelingResponse.addMessage("Add property [" + propertyName + "] to relationship [" + relationshipId.toString() + "] of relationship group [" + relationGroupName + "] successfully.");
                }
            }
            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
            relationNodeRepository.save(relationNode);

            while (relationNode.getId() == null) {
                System.out.println("@@ Id is null. Waiting for saving complete. (" + dbSavingDelay.toString() + " ms)");
                try {
                    Thread.sleep(dbSavingDelay);  // 暂停少时，防止 save 还没执行完，relationNode 中的 id 属性为 null
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            // 更新 id 映射和返回信息
            if (isFreshCreation) {
                modelingResponse.addIdMapping(relationshipId, relationNode.getId());  // 向返回对象中添加映射
                this.addFrontBackIdMapping(icm, relationshipId, relationNode.getId());  // 向 ICM 中添加映射
            }
            modelingResponse.addMessage("Add relationship [" + relationGroupName + "] successfully.");

        } else if (opV.equals("ADD") && opO.equals("ATT")) {  // 添加 attribute 的大操作

            // ADD ATT class attribute attributeCCMId addingType (fresh, binding)
            String className = op.get(3);
            String attributeName = op.get(4);
            String attributeId = op.get(5);
            String addingType = op.get(6);

            // 获取 class node （一定存在于 ICM 中）
            ClassNode cnMainClass = classNodeRepository.getOneByName(ccmId, icmId, className);
            assert cnMainClass != null;
            boolean isFreshCreation = addingType.equals("fresh");
            RelationNode relationNode;
            Map<String, ValueNode> valueNodePool = new HashMap<>();  // 用于缓存尚未存入数据库的值节点，避免重复创建值节点
            
            if (isFreshCreation) {  // 全新创建

                //  添加 (relationship)-[e0.class]->(class)
                relationNode = new RelationNode(ccmId, icmId);
                this.addR2CEdgeWithoutSaving(ccmId, icmId, "E0", "class", relationNode, cnMainClass);

                // 添加 (relationship)-[isAttribute]->(#true)
                this.addR2VEdgeWithoutSaving(ccmId, icmId, "", "isAttribute", relationNode, "#true", valueNodePool);

                // 添加 (relationship)-[e0.role]->(value)
                this.addR2VEdgeWithoutSaving(ccmId, icmId, "E0", "role", relationNode, Util.decapitalize(className), valueNodePool);

                // 添加 (relationship)-[e1.role]->(value)
                this.addR2VEdgeWithoutSaving(ccmId, icmId, "E1", "role", relationNode, attributeName, valueNodePool);

                for (List<String> smallOp : bigOp) {

                    System.out.print("## Operation: ( ccmId: " + ccmId.toString() + ", icmId: " + icmId.toString() + " ) [ADD ATT FRESH] ");
                    for (String opElem : smallOp) {  // 打印
                        System.out.print(opElem);
                        System.out.print(" ");
                    }
                    System.out.print("\n");

                    if (smallOp.get(2).equals("ATT")) continue;  // 跳过 ADD ATT 和 ODI ATT
                    if (smallOp.get(5).equals("name")) continue;  // 跳过 ADD POA xxx xxx name （已被 ADD ATT 处理）

                    // ADD POA class attribute property value
                    String propertyName = smallOp.get(5);
                    String propertyValueE1 = smallOp.get(6);

                    if (propertyName.equals("type")) {

                        // 作为类型的类
                        if (propertyValueE1.equals("int") || propertyValueE1.equals("float") || propertyValueE1.equals("string") || propertyValueE1.equals("boolean")) {
                            propertyValueE1 = "_" + propertyValueE1;  // 内置类型名称的特殊处理
                        }
                        ClassNode cnTypeClass = this.getClassNodeByNameWithoutSaving(ccmId, icmId, propertyValueE1, valueNodePool, relationNode, null);  // 返回类型类（若不存在则新建）
                        this.addR2CEdgeWithoutSaving(ccmId, icmId, "E1", "class", relationNode, cnTypeClass);  // 添加关系到类型类的边

                    } else {

                        // 一般 property
                        String propertyValueE0 = null;
                        switch (propertyName) {
                            case "multiplicity":
                                propertyValueE0 = "1";
                                break;
                            case "visibility":
                                propertyValueE0 = "public";
                                break;
                            case "default":
                            case "constraint":
                            case "subsets":
                            case "redefines":
                                propertyValueE0 = "_";
                                break;
                            case "ordering":
                            case "uniqueness":
                            case "readOnly":
                            case "union":
                            case "composite":
                                propertyValueE0 = "#true";
                                break;
                            default:
                                return;
                        }

                        // 添加 (relationship)-[e0.{propertyName}]->(value)
                        this.addR2VEdgeWithoutSaving(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);

                        // 添加 (relationship)-[e1.{propertyName}]->(value)
                        this.addR2VEdgeWithoutSaving(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);
                    }
                    modelingResponse.addMessage("Add property [" + propertyName + "] to attribute [" + attributeName + "] of class [" + className + "] successfully.");
                }

            } else {  // 绑定创建
                relationNode = relationNodeRepository.findOne(Long.parseLong(attributeId, 10), 2);
                this.solveASDNBug(relationNode);
                relationNode.addIcmId(icmId);
                relationNode.setIsSettled(false);  // 有待融合算法进一步处理

                //  添加 (relationship)-[e0.class]->(class) （此类必定存在于 ICM 中）
                for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
                    if (r2cEdge.getPort().equals("E0") && r2cEdge.getName().equals("class")
                            && r2cEdge.getEnder().getId().equals(cnMainClass.getId())) {
                        r2cEdge.addIcmId(icmId);
                        break;
                    }
                }

                // 添加 (relationship)-[isAttribute]->(#true)
                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "", "isAttribute", relationNode, "#true", valueNodePool);

                // 添加 (relationship)-[e0.role]->(value)
                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", "role", relationNode, Util.decapitalize(className), valueNodePool);

                // 添加 (relationship)-[e1.role]->(value)
                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", "role", relationNode, attributeName, valueNodePool);

                for (List<String> smallOp : bigOp) {

                    System.out.print("## Operation: ( ccmId: " + ccmId.toString() + ", icmId: " + icmId.toString() + " ) [ADD ATT FRESH] ");
                    for (String opElem : smallOp) {  // 打印
                        System.out.print(opElem);
                        System.out.print(" ");
                    }
                    System.out.print("\n");

                    if (smallOp.get(2).equals("ATT")) continue;  // 跳过 ADD ATT 和 ODI ATT
                    if (smallOp.get(5).equals("name")) continue;  // 跳过 ADD POA xxx xxx name （已被 ADD ATT 处理）

                    // ADD POA class attribute property value
                    String propertyName = smallOp.get(5);
                    String propertyValueE1 = smallOp.get(6);

                    if (propertyName.equals("type")) {

                        // 作为类型的类
                        if (propertyValueE1.equals("int") || propertyValueE1.equals("float") || propertyValueE1.equals("string") || propertyValueE1.equals("boolean")) {
                            propertyValueE1 = "_" + propertyValueE1;  // 内置类型名称的特殊处理
                        }
                        ClassNode cnTypeClass = this.getClassNodeByNameWithoutSaving(ccmId, icmId, propertyValueE1, valueNodePool, relationNode, null);  // 返回类型类（若不存在则新建）
                        this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", "class", relationNode, cnTypeClass);  // 添加关系到类型类的边

                    } else {

                        // 一般 property
                        String propertyValueE0 = null;
                        switch (propertyName) {
                            case "multiplicity":
                                propertyValueE0 = "1";
                                break;
                            case "visibility":
                                propertyValueE0 = "public";
                                break;
                            case "default":
                            case "constraint":
                            case "subsets":
                            case "redefines":
                                propertyValueE0 = "_";
                                break;
                            case "ordering":
                            case "uniqueness":
                            case "readOnly":
                            case "union":
                            case "composite":
                                propertyValueE0 = "#true";
                                break;
                            default:
                                return;
                        }

                        // 添加 (relationship)-[e0.{propertyName}]->(value)
                        this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);

                        // 添加 (relationship)-[e1.{propertyName}]->(value)
                        this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);
                    }
                    modelingResponse.addMessage("Add property [" + propertyName + "] to attribute [" + attributeName + "] of class [" + className + "] successfully.");
                }
            }
            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
            relationNodeRepository.save(relationNode);

            while (relationNode.getId() == null) {
                System.out.println("@@ Id is null. Waiting for saving complete. (" + dbSavingDelay.toString() + " ms)");
                try {
                    Thread.sleep(dbSavingDelay);  // 暂停少时，防止 save 还没执行完，relationNode 中的 id 属性为 null
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            // 更新 id 映射和返回信息
            if (isFreshCreation) {
                modelingResponse.addIdMapping(attributeId, relationNode.getId());  // 向返回对象中添加映射
                this.addFrontBackIdMapping(icm, attributeId, relationNode.getId());  // 向 ICM 中添加映射
            }
            modelingResponse.addMessage("Add attribute [" + attributeName + "] to class [" + className + "] successfully.");

        }
    }

    // 根据名字获取值节点，值节点可能不存在，需要进一步保存
    private Pair<ValueNode, Boolean> getValueNodeByNameWithoutSaving(Long ccmId, Long icmId, String name, Map<String, ValueNode> valueNodePool, RelationNode relationNode, ClassNode classNode) {

        // 优先检查是否刚刚使用了同名的值节点
        if (valueNodePool.containsKey(name)) {
            return new Pair<>(valueNodePool.get(name), true);
        }

        // 若传入了某 relationNode，则看其周围是否有要的 valueNode（很有必要，应对 SDN4 的一个 BUG：valueNode 已在 relationNode 周围，但若重新从 DB 提取，则该 valueNode 保存可能不及时）
        if (relationNode != null) {
            for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
                if (r2vEdge.getEnder().getName().equals(name)) {
                    r2vEdge.getEnder().addIcmId(icmId);  // 可能已经有 icmId 了，但加一下不亏
                    valueNodePool.put(name, r2vEdge.getEnder());  // 已在 relationNode 周围的 valueNode 也要加入池中
                    return new Pair<>(r2vEdge.getEnder(), true);
                }
            }
            for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
                ClassNode cn = r2cEdge.getEnder();
                for (ClassToValueEdge c2vEdge : cn.getCtvEdges()) {
                    if (c2vEdge.getEnder().getName().equals(name)) {
                        c2vEdge.getEnder().addIcmId(icmId);  // 可能已经有 icmId 了，但加一下不亏
                        valueNodePool.put(name, c2vEdge.getEnder());  // 已在 classNode 周围的 valueNode 也要加入池中
                        return new Pair<>(c2vEdge.getEnder(), true);
                    }
                }
            }
        }

        // 若传入了某 classNode，则看其周围是否有要的 valueNode（很有必要，应对 SDN4 的一个 BUG：valueNode 已在 relationNode 周围，但若重新从 DB 提取，则该 valueNode 保存可能不及时）
        if (classNode != null) {
            for (ClassToValueEdge c2vEdge : classNode.getCtvEdges()) {
                if (c2vEdge.getEnder().getName().equals(name)) {
                    c2vEdge.getEnder().addIcmId(icmId);  // 可能已经有 icmId 了，但加一下不亏
                    valueNodePool.put(name, c2vEdge.getEnder());  // 已在 classNode 周围的 valueNode 也要加入池中
                    return new Pair<>(c2vEdge.getEnder(), true);
                }
            }
        }

        // 上面几种情况都不是，则去 DB 中寻找
        List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, name);
        ValueNode valueNode;
        if (valueNodes.isEmpty()) {
            valueNode = new ValueNode(ccmId, icmId, name);
            valueNodePool.put(name, valueNode);  // 将刚创建的值节点加入池中
            return new Pair<>(valueNode, false);
        } else {
            Long valueNodeId = valueNodes.get(0).getId();
            valueNode = valueNodeRepository.findOne(valueNodeId, 1);  // 以深度 1 重新提取值节点
            valueNode.addIcmId(icmId);
            valueNodePool.put(name, valueNode);  // 将刚从 DB 中提取的值节点加入池中
            return new Pair<>(valueNode, true);
        }

    }
    
    // 根据名字获取类节点
    private ClassNode getClassNodeByNameWithoutSaving(Long ccmId, Long icmId, String name, Map<String, ValueNode> valueNodePool, RelationNode rn, ClassNode cn) {

        Pair<ValueNode, Boolean> valueNodeResults = this.getValueNodeByNameWithoutSaving(ccmId, icmId, name, valueNodePool, rn, cn);
        ValueNode valueNode = valueNodeResults.getKey();
        boolean valueNodeExistsInCCM = valueNodeResults.getValue();
        ClassNode classNode;
        ClassToValueEdge c2vEdge;

        // 1、类名的值节点就不存在，类节点一定也不存在，全部新建
        if (!valueNodeExistsInCCM) {
            classNode = new ClassNode(ccmId, icmId);
            c2vEdge = new ClassToValueEdge(ccmId, icmId, "name", classNode, valueNode);
            valueNode.addC2VEdge(c2vEdge);
            classNode.addC2VEdge(c2vEdge);

        } else {
            valueNode.addIcmId(icmId);  // 不管之前此值节点是否有 icmId，这里加一下总不会出错

            // 2、(class)-[edge]->(value) 完整的一套存在于 ICM 中
            for (ClassToValueEdge c2ve : valueNode.getCtvEdges()) {
                if (c2ve.getIcmSet().contains(icmId) && c2ve.getStarter().getIcmSet().contains(icmId)) {
                    return c2ve.getStarter();  // 注意，若 2 成立，则后面都不执行了
                }
            }

            // 3、(class)-[edge]->(value) 完整的一套不在 ICM 中，但存在于 CCM 中（类节点未出现在 ICM 中）
            for (ClassToValueEdge c2ve : valueNode.getCtvEdges()) {
                if (!c2ve.getIcmSet().contains(icmId) && !c2ve.getStarter().getIcmSet().contains(icmId)) {
                    classNode = c2ve.getStarter();
                    c2ve.addIcmId(icmId);
                    classNode.addIcmId(icmId);
                    return classNode;  // 注意，若 3 成立，则后面都不执行了
                }
            }

            // 4、值节点存在于 CCM 中，但类节点和边需要新建（要么是值节点周围没有类节点，要么是值节点周围的类节点都已存在于 ICM 中）
            classNode = new ClassNode(ccmId, icmId);
            c2vEdge = new ClassToValueEdge(ccmId, icmId, "name", classNode, valueNode);
            valueNode.addC2VEdge(c2vEdge);
            classNode.addC2VEdge(c2vEdge);
        }
        return classNode;
    }

    // 添加 relationship --> value 的边
    private void addR2VEdgeWithoutSaving(Long ccmId, Long icmId, String edgePort, String edgeName, RelationNode relationNode, String valueName, Map<String, ValueNode> valueNodePool) {
        ValueNode valueNode = getValueNodeByNameWithoutSaving(ccmId, icmId, valueName, valueNodePool, relationNode, null).getKey();
        RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, edgePort, edgeName, relationNode, valueNode);
        relationNode.addR2VEdge(r2vEdge);
        valueNode.addR2VEdge(r2vEdge);
    }

    // 添加 relationship --> value 的边（relationNode 是从数据库中以深度 2 提取的，要考虑边已存在于 CCM 的情况）
    private void addR2VEdgeWithoutSavingConsiderExistence(Long ccmId, Long icmId, String edgePort, String edgeName, RelationNode relationNode, String valueName, Map<String, ValueNode> valueNodePool) {
        boolean existsInCCM = false;
        for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
            if (r2vEdge.getPort().equals(edgePort)
                    && r2vEdge.getName().equals(edgeName)
                    && r2vEdge.getEnder().getName().equals(valueName)) {
                existsInCCM = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
                valueNodePool.put(valueName, r2vEdge.getEnder());  // 已在 relationNode 周围的 valueNode 也要加入池中
                break;
            }
        }
        if (!existsInCCM) {
            addR2VEdgeWithoutSaving(ccmId, icmId, edgePort, edgeName, relationNode, valueName, valueNodePool);
        }
    }

    //  添加 relationship --> class 的边
    private void addR2CEdgeWithoutSaving(Long ccmId, Long icmId, String edgePort, String edgeName, RelationNode relationNode, ClassNode classNode) {
        RelationToClassEdge r2cEdge = new RelationToClassEdge(ccmId, icmId, edgePort, edgeName, relationNode, classNode);
        relationNode.addR2CEdge(r2cEdge);
        classNode.addR2CEdge(r2cEdge);
        classNode.setIsSettled(false);
    }

    //  添加 relationship --> class 的边（relationNode 是从数据库中以深度 2 提取的，要考虑边已存在于 CCM 的情况）
    private void addR2CEdgeWithoutSavingConsiderExistence(Long ccmId, Long icmId, String edgePort, String edgeName, RelationNode relationNode, ClassNode classNode) {
        boolean existsInCCM = false;
        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            if (r2cEdge.getPort().equals(edgePort)
                    && r2cEdge.getName().equals(edgeName)
                    && r2cEdge.getEnder().getId().equals(classNode.getId())) {
                existsInCCM = true;
                r2cEdge.addIcmId(icmId);
                r2cEdge.getEnder().addIcmId(icmId);
                r2cEdge.getEnder().setIsSettled(false);
                break;
            }
        }
        if (!existsInCCM) {
            addR2CEdgeWithoutSaving(ccmId, icmId, edgePort, edgeName, relationNode, classNode);
        }
    }

    /**
     * 执行单条操作
     *
     * -- 日志条目的格式：
     *
     * - 增加
     * ADD CLS class classCCMId addingType (fresh, binding)
     * ADD RLG relationGroup
     * ADD ATT class attribute attributeCCMId addingType (fresh, binding)
     * ADD RLT relationGroup relation relationCCMId addingType (fresh, binding)
     * ADD POA class attribute property value
     * ADD POR relationGroup relation property value
     *
     * - 修改key
     * MOD CLS classOld classNew
     * MOD RLG relationGroupOld relationGroupNew
     * MOD ATT class attributeOld attributeNew
     * // MOD RLT relationGroup relationOld relationNew
     *
     * - 修改value
     * MOD POA class attribute property value
     * MOD POR relationGroup relation property value
     *
     * - 删除
     * RMV CLS class
     * RMV RLG relationGroup
     * RMV ATT class attribute
     * RMV RLT relationGroup relation
     * RMV POA class attribute property
     * RMV POR relationGroup relation property
     *
     * - 插入order元素
     * ODI ATT class attribute position direction
     * ODI RLT relationGroup relation position direction
     *
     * - 修改order元素名称
     * ODE ATT class attributeOld attributeNew
     * // ODE RLT relationGroup relationOld relationNew
     *
     * - 删除order元素
     * ODR ATT class attribute
     * ODR RLT relationGroup relation
     *
     * - 移动order元素
     * ODM ATT class attribute position direction
     * ODM RLT relationGroup relation position direction
     *
     * @param op 一条操作
     */
    @Transactional
    private void executeOP(List<String> op, Long ccmId, Long icmId, ModelingResponse modelingResponse, IndividualConceptualModel icm) {

        System.out.print("## Operation: ( ccmId: " + ccmId.toString() + ", icmId: " + icmId.toString() + " ) ");
        for (String opElem : op) {  // 打印
            System.out.print(opElem);
            System.out.print(" ");
        }
        System.out.print("\n");

        String opV = op.get(1);  // 谓语
        String opO = op.get(2);  // 宾语

        Map<String, ValueNode> valueNodePool = new HashMap<>();  // 用于缓存尚未存入数据库的值节点，避免重复创建值节点
        neo4jTemplate.clear();  // 操作前清空 session 中的 mappingContext，及其重要

        // 注意，op 的第一个元素是 Date
        switch (opV) {
            case "ADD":
                switch (opO) {
                    case "CLS": {  // add class

                        // ADD CLS class classCCMId addingType (fresh, binding)
                        String className = op.get(3);
                        String classId = op.get(4);
                        String addingType = op.get(5);
                        boolean isFreshCreation = addingType.equals("fresh");

                        if (isFreshCreation) {  // 全新创建，但有可能同名融合

                            ClassNode classNode = this.getClassNodeByNameWithoutSaving(ccmId, icmId, className, valueNodePool, null, null);
                            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                            classNodeRepository.save(classNode);

                            // 更新 id 映射和返回信息
                            modelingResponse.addIdMapping(classId, classNode.getId());  // 向返回对象中添加映射
                            this.addFrontBackIdMapping(icm, classId, classNode.getId());  // 向 ICM 中添加映射

                        } else {  // 推荐绑定

                            // 获取 class node
                            ClassNode classNode = classNodeRepository.findOne(Long.parseLong(classId, 10), 1);  // 深度 1，只取出周围的 valueNode(有用) 和 relationNode(没用)
                            classNode.addIcmId(icmId);
                            classNode.setIsSettled(false);  // 有待融合算法进一步处理
                            boolean isValueNodeAround = false;  // 考虑到用户采纳推荐时可能改名，因此名称为类名的 valueNode 不一定在类周围

                            // 优先检查 valueNode 是否就在该 classNode 周围
                            for (ClassToValueEdge c2vEdge : classNode.getCtvEdges()) {
                                if (c2vEdge.getEnder().getName().equals(className)
                                        && c2vEdge.getPort().equals("")
                                        && c2vEdge.getName().equals("name")) {  // 名称为类名的 valueNode 就在类周围
                                    isValueNodeAround = true;
                                    c2vEdge.addIcmId(icmId);
                                    c2vEdge.getEnder().addIcmId(icmId);
                                    break;
                                }
                            }

                            // 名称为类名的 valueNode 不在类周围
                            ValueNode valueNode;
                            ClassToValueEdge c2vEdge;
                            if (!isValueNodeAround) {
                                List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, className);

                                if (!valueNodes.isEmpty()) {  // valueNode 存在于 CCM，只是没有和该 classNode 相连
                                    valueNode = valueNodes.get(0);
                                    valueNode.addIcmId(icmId);
                                } else {  // valueNode 不存在于 CCM
                                    valueNode = new ValueNode(ccmId, icmId, className);
                                }
                                c2vEdge = new ClassToValueEdge(ccmId, icmId, "name", classNode, valueNode);
                                classNode.addC2VEdge(c2vEdge);
                                valueNode.addC2VEdge(c2vEdge);
                            }
                            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                            classNodeRepository.save(classNode);
                        }
                        modelingResponse.addMessage("Add class [" + className + "] successfully.");

                        break;
                    }
                    case "RLG":   // add relation group

                        // DO NOTHING

                        break;
                    case "ATT": {  // add attribute

                        // handled by BigOps

                        break;
                    }
                    case "RLT": {  // add relationship

                        // handled by BigOps

                        break;
                    }
                    case "POA": {  // add property of attribute

                        // ADD POA class attribute property value
                        String className = op.get(3);
                        String attributeName = op.get(4);
                        String propertyName = op.get(5);
                        String propertyValueE1 = op.get(6);

                        // name 这个 property 已经在 ADD ATT 操作中处理了，因此这里直接无视
                        if (propertyName.equals("name")) return;

                        // 获取 relationship node
                        RelationNode relationNode = relationNodeRepository.getOneAttRelByClassNameAndAttName(ccmId, icmId, className, attributeName);
                        assert relationNode != null;
                        relationNode = relationNodeRepository.findOne(relationNode.getId(), 2);
                        this.solveASDNBug(relationNode);
                        relationNode.setIsSettled(false);  // 有待融合算法进一步处理

                        if (propertyName.equals("type")) {

                            // 作为类型的类
                            if (propertyValueE1.equals("int") || propertyValueE1.equals("float") || propertyValueE1.equals("string") || propertyValueE1.equals("boolean")) {
                                propertyValueE1 = "_" + propertyValueE1;  // 内置类型名称的特殊处理
                            }
                            ClassNode cnTypeClass = this.getClassNodeByNameWithoutSaving(ccmId, icmId, propertyValueE1, valueNodePool, relationNode, null);  // 返回类型类（若不存在则新建）
                            this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", "class", relationNode, cnTypeClass);  // 添加关系到类型类的边

                        } else {

                            // 一般 property
                            String propertyValueE0 = null;
                            switch (propertyName) {
                                case "multiplicity":
                                    propertyValueE0 = "1";
                                    break;
                                case "visibility":
                                    propertyValueE0 = "public";
                                    break;
                                case "default":
                                case "constraint":
                                case "subsets":
                                case "redefines":
                                    propertyValueE0 = "_";
                                    break;
                                case "ordering":
                                case "uniqueness":
                                case "readOnly":
                                case "union":
                                case "composite":
                                    propertyValueE0 = "#true";
                                    break;
                                default:
                                    return;
                            }

                            // 添加 (relationship)-[e0.{propertyName}]->(value)
                            this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);

                            // 添加 (relationship)-[e1.{propertyName}]->(value)
                            this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);
                        }

                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        relationNodeRepository.save(relationNode);
                        modelingResponse.addMessage("Add property [" + propertyName + "] to attribute [" + attributeName + "] of class [" + className + "] successfully.");

                        break;
                    }
                    case "POR": {  // add property of relationship

                        // ADD POR relationGroup relation property valueE0 valueE1
                        String relationGroupName = op.get(3);
                        Long relationshipId = this.getNeo4jId(icmId, op.get(4));  // NOTE: may be FrontId or Neo4jId !!!

                        String propertyName = op.get(5);
                        String propertyValueE0 = op.get(6);
                        String propertyValueE1 = op.get(7);

                        // 获取关系节点（必定存在于 ICM 中）
                        RelationNode relationNode = relationNodeRepository.findOne(relationshipId, 2);
                        assert relationNode != null;
                        this.solveASDNBug(relationNode);
                        relationNode.setIsSettled(false);  // 有待融合算法进一步处理

                        switch (propertyName) {
                            case "type": {  // 关系的类型和名称（名称可能为空字符串）

                                // 添加关系类型
                                String typeEdgeName = "is" + propertyValueE0;
                                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "", typeEdgeName, relationNode, "#true", valueNodePool);

                                // 若有关系名字，则添加
                                if (!propertyValueE1.equals("")) {
                                    this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "", "name", relationNode, propertyValueE1, valueNodePool);
                                }

                                break;
                            }
                            case "class": {  // 关系两端的类 (必定存在于 ICM 中)

                                // 获取关系两端的 class node (必定存在于 ICM 中)
                                ClassNode classNodeE0 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE0);
                                ClassNode classNodeE1 = classNodeRepository.getOneByName(ccmId, icmId, propertyValueE1);
                                assert classNodeE0 != null && classNodeE1 != null;

                                // 添加关系到类型类的边
                                this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", "class", relationNode, classNodeE0);
                                this.addR2CEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", "class", relationNode, classNodeE1);

                                break;
                            }
                            default: {  // 一般 property

                                // 添加 (relationship)-[e0.{propertyName}]->(value)
                                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E0", propertyName, relationNode, propertyValueE0, valueNodePool);

                                // 添加 (relationship)-[e1.{propertyName}]->(value)
                                this.addR2VEdgeWithoutSavingConsiderExistence(ccmId, icmId, "E1", propertyName, relationNode, propertyValueE1, valueNodePool);

                                break;
                            }
                        }

                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        relationNodeRepository.save(relationNode);
                        modelingResponse.addMessage("Add property [" + propertyName + "] to relationship [" + relationshipId.toString() + "] of relationship group [" + relationGroupName + "] successfully.");

                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Operation " + opV + " " + opO + " is not supported");  // NOT ALLOWED

                }

                break;
            case "RMV":
                switch (opO) {
                    case "CLS": {  // remove a class

                        // RMV CLS class
                        String className = op.get(3);

                        // 删除与 class 节点相连的 relationship （attribute 可与 relationship 一视同仁，只要提供 attribute 的 ID 即可）
                        // RMV CLS 之前，会自动生成与该 class 相关的 RMV RLG （在之前的 OP 中），因此下面要删除的 relationship 都是 attribute
                        List<Long> relationshipIds = relationNodeRepository.getAllRelationshipIdsRelatedToClass(icmId, className);
                        for (Number relationshipId : relationshipIds) {  // 这里的 Number 类型和下面的 longValue() 方法调用是为了应付 SDN4 的一个 Long 变为 Integer 的 BUG
                            this.removeAttributeOrRelationship(ccmId, icmId, "relationship", "", "", relationshipId.longValue());
                        }

                        // 删除 class 节点
                        ClassNode classNode = classNodeRepository.getOneByName(ccmId, icmId, className);
                        assert classNode != null;
                        classNode = classNodeRepository.findOne(classNode.getId(), 2);  // class name 在一个 ICM 中不会重复，但可能作为 role，因此仍需观察存储类名的 value node 周围的边后才能删除
                        for (ClassToValueEdge c2vEdge: classNode.getCtvEdges()) {
                            if (c2vEdge.getIcmSet().contains(icmId) && c2vEdge.getName().equals("name")) {
                                c2vEdge.removeIcmId(icmId);
                                c2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
                                break;
                            }
                        }
                        classNode.removeIcmId(icmId);
                        classNode.setIsSettled(false);  // 有待融合算法进一步处理
                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        classNodeRepository.save(classNode);

                        // 删除相应的 Order 节点
                        orderRepository.removeByIcmIdAndName(icmId, className);
                        modelingResponse.addMessage("Remove class [" + className + "] successfully.");

                        break;
                    }
                    case "ATT": {  // remove an attribute

                        // RMV ATT class attribute
                        String className = op.get(3);
                        String attributeName = op.get(4);

                        this.removeAttributeOrRelationship(ccmId, icmId, "attribute", className, attributeName, 0L);
                        modelingResponse.addMessage("Remove attribute [" + attributeName + "] " +
                                "from class [" + className + "] successfully.");

                        break;
                    }
                    case "POA": {  // remove a property of an attribute

                        // RMV POA class attribute property
                        String className = op.get(3);
                        String attributeName = op.get(4);
                        String propertyName = op.get(5);

                        this.removeProperty(ccmId, icmId, "attribute", className, attributeName, 0L, propertyName);
                        modelingResponse.addMessage("Remove property [" + propertyName + "] " +
                                "from attribute [" + attributeName + "] " +
                                "of class [" + className + "] successfully.");

                        break;
                    }
                    case "RLG": {  // remove a relationship group

                        // RMV RLG relationGroup
                        String relationshipGroupName = op.get(3);
                        String[] classNames = relationshipGroupName.split("-");

                        // 删除所有在该 relationship group 中的 relationship
                        List<Long> relationshipIds = relationNodeRepository.getAllRelationshipIdsInRelationshipGroup(icmId, classNames[0], classNames[1]);
                        for (Number relationshipId : relationshipIds) {  // 这里的 Number 类型和下面的 longValue() 方法调用是为了应付 SDN4 的一个 Long 变为 Integer 的 BUG
                            this.removeAttributeOrRelationship(ccmId, icmId, "relationship", "", "", relationshipId.longValue());
                        }

                        // 删除与该 relationship group 对应的 Order 节点
                        orderRepository.removeByIcmIdAndName(icmId, relationshipGroupName);
                        modelingResponse.addMessage("Remove relationship group [" + relationshipGroupName + "] successfully.");

                        break;
                    }
                    case "RLT": {  // remove a relationship

                        // RMV RLT relationGroup relation
                        String relationshipGroupName = op.get(3);
                        Long relationshipId = this.getNeo4jId(icmId, op.get(4));

                        this.removeAttributeOrRelationship(ccmId, icmId, "relationship", "", "", relationshipId);
                        modelingResponse.addMessage("Remove relationship [" + relationshipId.toString() + "] " +
                                "from relationship group [" + relationshipGroupName + "] successfully.");

                        break;
                    }
                    case "POR": {  // remove a property of a relationship

                        // RMV POR relationGroup relation property
                        String relationshipGroupName = op.get(3);
                        Long relationshipId = this.getNeo4jId(icmId, op.get(4));
                        String propertyName = op.get(5);

                        this.removeProperty(ccmId, icmId, "relationship", "", "", relationshipId, propertyName);
                        modelingResponse.addMessage("Remove property [" + propertyName + "] " +
                                "from relationship [" + relationshipId.toString() + "] " +
                                "of relationship group [" + relationshipGroupName + "] successfully.");

                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Operation " + opV + " " + opO + " is not supported");  // NOT ALLOWED
                }
                break;
            case "MOD":
                switch (opO) {
                    case "CLS": {  // modify the name of a class

                        // MOD CLS classOld classNew
                        String classNameOld = op.get(3);
                        String classNameNew = op.get(4);

                        // 更换类所连接的 value 节点
                        ClassNode classNode = classNodeRepository.getOneByName(ccmId, icmId, classNameOld);
                        assert classNode != null;
                        classNode = classNodeRepository.findOne(classNode.getId(), 2);
                        // class name 在一个 ICM 中不会重复，因此不必观察存储类名的 value node 周围的边即可直接删除（此句仅针对英文项目成立，对于中文项目，则类名和角色名可能重复）
                        // 为了兼顾中文项目，这里提取深度为 2

                        boolean newClassNameAlreadyConnected = false;  // 标志位，表示新类名的 value 点是否已经在 CCM 中与 class 点连接
                        for (ClassToValueEdge c2vEdge: classNode.getCtvEdges()) {
                            if (c2vEdge.getIcmSet().contains(icmId) && c2vEdge.getName().equals("name") && c2vEdge.getEnder().getName().equals(classNameOld)) {  // 删除旧名

                                // 删除边
                                c2vEdge.removeIcmId(icmId);

                                // 判断该类名是否被某个关系作为角色名使用后再删除值节点
                                ValueNode valueNode = c2vEdge.getEnder();
                                boolean usedAsRoleName = false;
                                for (RelationToValueEdge r2vEdge : valueNode.getRtvEdges()) {
                                    if (r2vEdge.getIcmSet().contains(icmId)) {  // 若该值节点至少被该 ICM 中一个关系作为角色名使用
                                        usedAsRoleName = true;
                                        break;
                                    }
                                }
                                if (!usedAsRoleName) {  // 若该值节点至少被该 ICM 中一个关系作为角色名使用，则不能删除；否则删除
                                    c2vEdge.getEnder().removeIcmId(icmId);
                                }

                            } else if (c2vEdge.getName().equals("name") && c2vEdge.getEnder().getName().equals(classNameNew)) {  // 若新名称已在 CCM 中与该类连接，则直接利用
                                newClassNameAlreadyConnected = true;
                                c2vEdge.addIcmId(icmId);
                                c2vEdge.getEnder().addIcmId(icmId);
                            }
                        }
                        if (!newClassNameAlreadyConnected) {  // 若新名称没有在 CCM 中与该类连接
                            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, classNameNew);
                            ValueNode valueNode;
                            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                                valueNode = valueNodes.get(0);
                                valueNode.addIcmId(icmId);
                            } else {  // 新名称并不存在于 CCM，则新建
                                valueNode = new ValueNode(ccmId, icmId, classNameNew);
                            }
                            ClassToValueEdge c2vEdge = new ClassToValueEdge(ccmId, icmId, "name", classNode, valueNode);
                            classNode.addC2VEdge(c2vEdge);
                            valueNode.addC2VEdge(c2vEdge);
                        }
                        classNode.setIsSettled(false);  // 有待融合算法进一步处理
                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        classNodeRepository.save(classNode);

                        // 修改 Attribute Order 节点（如果存在的话）
                        List<Order> attributeOrders = orderRepository.getByIcmIdAndTypeAndName(icmId, "AttOdr", classNameOld);
                        if (!attributeOrders.isEmpty()) {
                            Order attributeOrder = attributeOrders.get(0);
                            attributeOrder.setName(classNameNew);
                            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                            orderRepository.save(attributeOrder);
                        }
                        modelingResponse.addMessage("Modify class name from [" + classNameOld + "] to [" + classNameNew + "]  successfully.");

                        break;
                    }
                    case "ATT": {  // modify the name of an attribute

                        // MOD ATT class attributeOld attributeNew
                        String className = op.get(3);
                        String attributeNameOld = op.get(4);
                        String attributeNameNew = op.get(5);

                        // DO NOTHING (handled by MOD POA)

                        modelingResponse.addMessage("Modify attribute name from [" + attributeNameOld + "] to [" + attributeNameNew + "] in class [" + className + "] successfully.");

                        break;
                    }
                    case "POA": {  // modify the property value of an attribute

                        // MOD POA class attribute property value
                        String className = op.get(3);
                        String attributeName = op.get(4);
                        String propertyName = op.get(5);
                        String propertyValueE1 = op.get(6);

                        // 获取 relationship node
                        RelationNode relationNode = relationNodeRepository.getOneAttRelByClassNameAndAttName(ccmId, icmId, className, attributeName);
                        assert relationNode != null;
                        relationNode = relationNodeRepository.findOne(relationNode.getId(), 2);
                        this.solveASDNBug(relationNode);

                        if (propertyName.equals("type")) {
                            this.modifyAttributeTypeProperty(ccmId, icmId, relationNode, propertyValueE1);

                        } else {  // 非 class 特性 (propertyName != "type")
                            this.modifyAttributeProperty(ccmId, icmId, relationNode, propertyName, propertyValueE1);
                        }

                        relationNode.setIsSettled(false);  // 有待融合算法进一步处理
                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        relationNodeRepository.save(relationNode);
                        modelingResponse.addMessage("Modify property [" + propertyName + "] " +
                                "to [" + propertyValueE1 + "] " +
                                "in attribute [" + attributeName + "] " +
                                "of class [" + className + "] successfully.");

                        break;
                    }
                    case "RLG": {  // modify the name of a relationshipGroup

                        // MOD RLG relationGroupOld relationGroupNew
                        String relationshipGroupNameOld = op.get(3);
                        String relationshipGroupNameNew = op.get(4);

                        List<Order> relationshipOrders = orderRepository.getByIcmIdAndTypeAndName(icmId, "RelOdr", relationshipGroupNameOld);
                        if (!relationshipOrders.isEmpty()) {
                            Order attributeOrder = relationshipOrders.get(0);
                            attributeOrder.setName(relationshipGroupNameNew);
                            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                            orderRepository.save(attributeOrder);
                        }
                        modelingResponse.addMessage("Modify relationship group name from [" + relationshipGroupNameOld + "] " +
                                "to [" + relationshipGroupNameNew + "]  successfully.");

                        break;
                    }
                    case "RLT": {
                        // DO NOTHING
                        break;
                    }
                    case "POR": {  // modify the property value of a relationship

                        // MOD POR relationGroup relation property valueE0 valueE1
                        String relationshipGroupName = op.get(3);
                        Long relationshipId = this.getNeo4jId(icmId, op.get(4));
                        String propertyName = op.get(5);
                        String propertyValueE0 = op.get(6);
                        String propertyValueE1 = op.get(7);

                        // 获取关系节点
                        RelationNode relationNode = relationNodeRepository.findOne(relationshipId, 2);
                        this.solveASDNBug(relationNode);
                        switch (propertyName) {
                            case "type":  // 关系类型和关系名
                                this.modifyRelationshipTypeProperty(ccmId, icmId, relationNode, propertyValueE0, propertyValueE1);
                                break;
                            case "class":  // 关系两端类换位
                                this.modifyRelationshipClassProperty(ccmId, icmId, relationNode, propertyValueE0, propertyValueE1);
                                break;
                            default:  // 其他 property
                                this.modifyRelationshipProperty(ccmId, icmId, relationNode, propertyName, propertyValueE0, propertyValueE1);
                        }
                        relationNode.setIsSettled(false);  // 有待融合算法进一步处理
                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        relationNodeRepository.save(relationNode);

                        modelingResponse.addMessage("Modify property [" + propertyName + "] " +
                                "to [" + propertyValueE0 + "-" + propertyValueE1 + "] " +
                                "in relationship [" + relationshipId.toString() + "] " +
                                "of relationshipGroup [" + relationshipGroupName + "] successfully.");

                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Operation " + opV + " " + opO + " is not supported");  // NOT ALLOWED
                }
                break;
            case "UPD":
                switch (opO) {
                    case "NUM": {  // update numbers 操作序列结束的标志

                        Long classNumInCcm = classNodeRepository.getClassNumInCcm(ccmId);
                        Long classNumInIcm = classNodeRepository.getClassNumInIcm(icmId);
                        Long relationshipNumInCcm = relationNodeRepository.getRelationshipNumInCcm(ccmId);
                        Long relationshipNumInIcm = relationNodeRepository.getRelationshipNumInIcm(icmId);

                        // 注意，这里不能去 DB 重新查询 ccm，否则会引发内存中存在两个 ccm 的 bug
                        CollectiveConceptualModel ccm = icm.getCcms().iterator().next();
                        ccm.updateNums(classNumInCcm, relationshipNumInCcm);
                        icm.updateNums(classNumInIcm, relationshipNumInIcm);
                        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
                        icmRepository.save(icm);

                        break;
                    }
                    default:
                        throw new IllegalArgumentException("Operation " + opV + " " + opO + " is not supported");  // NOT ALLOWED
                }
                break;
            default:
                // DO NOTHING
                break;
        }
    }

    // 应对 SDN4 的一个错误放置边的 BUG
    private void solveASDNBug(RelationNode relationNode) {
        System.out.println("@@ -- Start solving SDN BUG --");
        relationNode.getRtcEdges();
        relationNode.getRtvEdges();
        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            ClassNode cn = r2cEdge.getEnder();
            cn.getCtvEdges();
            cn.getRtcEdges();
        }
        for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
            ValueNode vn = r2vEdge.getEnder();
            vn.getCtvEdges();
            vn.getRtvEdges();
        }
        System.out.println("@@ -- Finish solving SDN BUG -- ");
    }

    /**
     * 由前端 ID 获得相应的后端 ID
     * @param icmId ICM ID
     * @param frontId 前端临时 ID
     * @return 后端数据库 ID
     */
    @Transactional
    private Long getBackIdFromFrontId(Long icmId, String frontId) {
        IndividualConceptualModel icm = icmRepository.findOne(icmId);
        return icm.getBackIdFromFrontId(frontId);
    }

    /**
     * 由前端 ID 获得相应的后端 ID （批量）
     * @param icmId ICM ID
     * @param frontIds 前端临时 ID List
     * @return 后端数据库 ID List
     */
    @Transactional
    private List<String> getBackIdsFromFrontIds(Long icmId, List<String> frontIds) {  // 因为是List，由于Neo4j的bug，需要用String而不是Long类型来表示BackId
        IndividualConceptualModel icm = icmRepository.findOne(icmId);
        List<String> backIds = new ArrayList<>();
        for (String frontId : frontIds) {
            backIds.add(icm.getBackIdFromFrontId(frontId).toString());
        }
        return backIds;
    }

    /**
     * 向 ICM 中添加 ID 映射
     * @param icmId ICM ID
     * @param frontId 前端临时 ID
     * @param backId 后端数据库 ID
     */
    @Transactional
    private void addFrontBackIdMapping(IndividualConceptualModel icm, String frontId, Long backId) {
        icm.addIdMapping(frontId, backId);
        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
        icmRepository.save(icm);
    }

    /**
     * 由 FrontId 获得 Neo4jId。若本来就是 Neo4jId，则直接转成 Long 类型
     * @param icmId icmId，id mapping 即保存在其中
     * @param id may be FrontId or Neo4jId !!!
     * @return Neo4jId
     */
    private Long getNeo4jId(Long icmId, String id) {
        return this.getBackIdFromFrontId(icmId, id);
    }

    /**
     * 根据 name 从数据库中获取 Order 对象，若不存在则创建后返回
     * @param name Order 的名称
     * @return Order 对象
     */
    private Order getOrder(Long icmId, String type, String name) {
        List<Order> Orders = orderRepository.getByIcmIdAndTypeAndName(icmId, type, name);
        if (!Orders.isEmpty()) {
            return Orders.get(0);
        } else {
            Order order = new Order(icmId, type, name);
            neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
            orderRepository.save(order);
            return order;
        }
    }

    /**
     * 从 ICM 中移除一个 attribute property 或 relationship property
     * @param ccmId ccmId
     * @param icmId icmId
     * @param type "attribute" or "relationship"
     * @param className 类名
     * @param attributeName 属性名
     * @param relationshipId 关系ID
     * @param propertyName 要移除的特性的名称
     */
    private void removeProperty(Long ccmId, Long icmId, String type, String className, String attributeName, Long relationshipId ,String propertyName) {

        // 获取 relationship node
        RelationNode relationNode;
        if (type.equals("attribute")) {
            relationNode = relationNodeRepository.getOneAttRelByClassNameAndAttName(ccmId, icmId, className, attributeName);
            relationNode = relationNodeRepository.findOne(relationNode.getId(), 2);  // 以距离 2 重新载入
        } else {  // relationship
            relationNode = relationNodeRepository.findOne(relationshipId, 2);  // 以距离 2 载入
        }
        this.solveASDNBug(relationNode);

        if (propertyName.equals("type")) {  // 要删除 attribute 的 type property，只需删除一条 E1.class 边 （relationship 不会删除 type property）

            // 获取关系的 E1.class property 边
            RelationToClassEdge r2cEdgeE1 = null;
            for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
                if (r2cEdge.getIcmSet().contains(icmId) && r2cEdge.getName().equals("class") && r2cEdge.getPort().equals("E1")) {
                    r2cEdgeE1 = r2cEdge;
                }
            }
            assert r2cEdgeE1 != null;

            // 删除其中的 icmId
            r2cEdgeE1.removeIcmId(icmId);

        } else {  // 要删除 type 以外的 property，需删 E0.{propertyName} 边和 E1.{propertyName} 边，及可能删除两边的端点

            // 获取关系的两条相应的 property 边 （propertyName 不为 "type"）
            RelationToValueEdge r2vEdgeE0 = null;
            RelationToValueEdge r2vEdgeE1 = null;
            for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
                if (r2vEdge.getIcmSet().contains(icmId) && r2vEdge.getName().equals(propertyName)) {
                    if (r2vEdge.getPort().equals("E0")) {
                        r2vEdgeE0 = r2vEdge;
                    } else {
                        r2vEdgeE1 = r2vEdge;
                    }
                }
            }
            assert r2vEdgeE0 != null && r2vEdgeE1 != null;

            // 删除其中的 icmId
            r2vEdgeE0.removeIcmId(icmId);
            r2vEdgeE1.removeIcmId(icmId);

            // 获取两边终点的 value nodes 并尝试删除其中的 icmId
            r2vEdgeE0.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
            r2vEdgeE1.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
        }

        relationNode.setIsSettled(false);  // 有待融合算法进一步处理
        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
        relationNodeRepository.save(relationNode);
    }

    /**
     * 从 ICM 中移除一个 attribute 或 relationship
     * @param ccmId ccmId
     * @param icmId icmId
     * @param type "attribute" or "relationship"
     * @param className 类名
     * @param attributeName 属性名
     * @param relationshipId 关系ID
     */
    private void removeAttributeOrRelationship(Long ccmId, Long icmId, String type, String className, String attributeName, Long relationshipId) {

        // 获取 relationship node
        RelationNode relationNode;
        if (type.equals("attribute")) {
            relationNode = relationNodeRepository.getOneAttRelByClassNameAndAttName(ccmId, icmId, className, attributeName);
            relationNode = relationNodeRepository.findOne(relationNode.getId(), 2);  // 以距离 2 重新载入
        } else {  // relationship
            relationNode = relationNodeRepository.findOne(relationshipId, 2);  // 以距离 2 载入
        }
        this.solveASDNBug(relationNode);

        // 删除所有 relationship 节点周围的 R2V 边及节点
        for (RelationToValueEdge r2vEdge : relationNode.getRtvEdges()) {
            if (r2vEdge.getIcmSet().contains(icmId)) {
                r2vEdge.removeIcmId(icmId);  // 删除其中的 icmId
                r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);  // 获取两边终点的 value nodes 并尝试删除其中的 icmId
            }
        }

        // 删除所有 relationship 节点周围的 R2C 边
        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            if (r2cEdge.getIcmSet().contains(icmId)) {
                r2cEdge.removeIcmId(icmId);  // 删除其中的 icmId
            }
        }

        // 删除 relationship 节点本身
        relationNode.removeIcmId(icmId);
        relationNode.setIsSettled(false);  // 有待融合算法进一步处理
        neo4jTemplate.clear();  // 清除 neo4j session 中的 mappingContext，以确保保存正确
        relationNodeRepository.save(relationNode);
    }

    /**
     * 修改属性中的特性的值（type 特性除外）
     * @param ccmId ccmId
     * @param icmId icmId
     * @param relationNode relationship node 提取深度需要大于等于 2
     * @param propertyName 特性名
     * @param propertyValue 特性值
     */
    private void modifyAttributeProperty(Long ccmId, Long icmId, RelationNode relationNode, String propertyName, String propertyValue) {  // relationship node 提取深度需要大于等于 2

        if (propertyName.equals("name")) {  // 对于 attribute name property，要换名成 role
            propertyName = "role";
        }
        boolean newAttributePropertyValueAlreadyConnected = false;  // 标志位，表示新属性名的 value 点是否已经在 CCM 中与 relationship 点连接
        for (RelationToValueEdge r2vEdge: relationNode.getRtvEdges()) {
            if (r2vEdge.getIcmSet().contains(icmId)
                    && r2vEdge.getPort().equals("E1")
                    && r2vEdge.getName().equals(propertyName)) {  // 删除旧名（ICM 中此点的 E1.{propertyName} 边唯一，因此不用进一步验证类型类的名字）
                r2vEdge.removeIcmId(icmId);
                r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
            } else if (r2vEdge.getPort().equals("E1")
                    && r2vEdge.getName().equals(propertyName)
                    && r2vEdge.getEnder().getName().equals(propertyValue)) {  // 若新名称已在 CCM 中与该 relationship 连接，则直接利用
                newAttributePropertyValueAlreadyConnected = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
            }
        }
        if (!newAttributePropertyValueAlreadyConnected) {  // 若新名称没有在 CCM 中与该 relationship 连接
            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, propertyValue);
            ValueNode valueNode;
            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                valueNode = valueNodes.get(0);
                valueNode.addIcmId(icmId);
            } else {  // 新名称并不存在于 CCM，则新建
                valueNode = new ValueNode(ccmId, icmId, propertyValue);
            }
            RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, "E1", propertyName, relationNode, valueNode);
            relationNode.addR2VEdge(r2vEdge);
            valueNode.addR2VEdge(r2vEdge);
        }
    }

    /**
     * 修改属性中的 type 特性的值
     * @param ccmId ccmId
     * @param icmId icmId
     * @param relationNode relationship node 提取深度需要大于等于 2
     * @param propertyValue 特性值
     */
    private void modifyAttributeTypeProperty(Long ccmId, Long icmId, RelationNode relationNode, String propertyValue) {

        // 作为类型的类
        if (propertyValue.equals("int") || propertyValue.equals("float") || propertyValue.equals("string") || propertyValue.equals("boolean")) {
            propertyValue = "_" + propertyValue;  // 内置类型名称的特殊处理
        }

        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            if (r2cEdge.getIcmSet().contains(icmId)
                    && r2cEdge.getPort().equals("E1")
                    && r2cEdge.getName().equals("class")) {  // 找到类型类，删除联系边（ICM 中此点的 E1.class 边唯一，因此不用进一步验证类型类的名字）
                r2cEdge.removeIcmId(icmId);
                break;
            }
        }

        boolean classNodeAlreadyConnected = false;
        RelationToClassEdge r2cEdge= null;
        ClassNode classNode= null;
        ClassToValueEdge c2vEdge = null;
        ValueNode valueNode = null;

        for (RelationToClassEdge edge : relationNode.getRtcEdges()) {
            if (edge.getPort().equals("E1") && edge.getName().equals("class")) {  //  && r2cEdge.getEnder().getIcmSet().contains(icmId)
                classNode = edge.getEnder();
                for (ClassToValueEdge edge2 : classNode.getCtvEdges()) {
                    if (edge2.getName().equals("name") && edge2.getEnder().getName().equals(propertyValue)) {  // CCM 中存在 (r)-->(c)-->(v:{name:{propertyValueE1}})
                        classNodeAlreadyConnected = true;
                        valueNode = edge2.getEnder();
                        r2cEdge = edge;
                        c2vEdge = edge2;
                        break;
                    }
                }
            }
            if (classNodeAlreadyConnected) break;
        }
        if (!classNodeAlreadyConnected) {
            List<ClassNode> classNodes = classNodeRepository.getByNameFromCcm(ccmId, propertyValue);
            if (!classNodes.isEmpty()) {  // CCM 中存在 (c)-->(v:{name:{propertyValueE1}})
                classNode = classNodeRepository.findOne(classNodes.get(0).getId(), 1);  // CCM 中符合条件的 classNode 可能有多个，取第一个。getByNameFromCcm() 可保证第一个是引用数最多的一个
                for (ClassToValueEdge edge : classNode.getCtvEdges()) {
                    if (edge.getEnder().getName().equals(propertyValue)) {
                        valueNode = edge.getEnder();
                        c2vEdge = edge;
                        break;
                    }
                }
            } else {  // CCM 中不存在 (c)-->(v:{name:{propertyValueE1}})  （认为 CCM 中不可能存在没有 class 节点连接的名字为类名（Axxxx or _xxxx）的 value 节点）
                classNode = new ClassNode(ccmId, icmId);
                valueNode = new ValueNode(ccmId, icmId, propertyValue);
                c2vEdge = new ClassToValueEdge(ccmId, icmId, "name", classNode, valueNode);
                classNode.addC2VEdge(c2vEdge);
                valueNode.addC2VEdge(c2vEdge);
            }
            r2cEdge = new RelationToClassEdge(ccmId, icmId, "E1", "class", relationNode, classNode);
            relationNode.addR2CEdge(r2cEdge);
            classNode.addR2CEdge(r2cEdge);
        }
        assert valueNode != null;
        valueNode.addIcmId(icmId);
        c2vEdge.addIcmId(icmId);
        classNode.addIcmId(icmId);
        classNode.setIsSettled(false);  // 有待融合算法进一步处理
        r2cEdge.addIcmId(icmId);
    }

    /**
     * 修改某 relationship 节点两端某个特性的值（不含 type 和 class 特性）
     * @param ccmId ccmId
     * @param icmId icmId
     * @param relationNode relationship node 提取深度需要大于等于 2
     * @param propertyName 特性名称
     * @param propertyValueE0 E0 端的特性值
     * @param propertyValueE1 E1 端的特性值
     */
    private void modifyRelationshipProperty(Long ccmId, Long icmId, RelationNode relationNode, String propertyName, String propertyValueE0, String propertyValueE1) {

        boolean isValueE0Modified = true;  // 假设 E0 端的值的确被修改了
        boolean isValueE1Modified = true;  // 假设 E1 端的值的确被修改了
        boolean newPropertyValue0AlreadyConnected = false;  // 标志位，表示新属性名的 value 点是否已经在 CCM 中与 relationship 点连接
        boolean newPropertyValue1AlreadyConnected = false;  // 标志位，表示新属性名的 value 点是否已经在 CCM 中与 relationship 点连接

        for (RelationToValueEdge r2vEdge: relationNode.getRtvEdges()) {
            // E0
            if (isValueE0Modified && r2vEdge.getIcmSet().contains(icmId) && r2vEdge.getPort().equals("E0") && r2vEdge.getName().equals(propertyName)) {  // 删除旧名
                if (r2vEdge.getEnder().getName().equals(propertyValueE0)) {  // E0 的 value 没有修改
                    isValueE0Modified = false;
                } else {  // E0 的 value 被修改了
                    r2vEdge.removeIcmId(icmId);
                    r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
                }
            } else if (isValueE0Modified && r2vEdge.getPort().equals("E0") && r2vEdge.getName().equals(propertyName)
                    && r2vEdge.getEnder().getName().equals(propertyValueE0)) {  // 若新名称已在 CCM 中与该 relationship 连接，则直接利用
                newPropertyValue0AlreadyConnected = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
            }
            // E1
            if (isValueE1Modified && r2vEdge.getIcmSet().contains(icmId) && r2vEdge.getPort().equals("E1") && r2vEdge.getName().equals(propertyName)) {  // 删除旧名
                if (r2vEdge.getEnder().getName().equals(propertyValueE1)) {  // E1 的 value 没有修改
                    isValueE1Modified = false;
                } else {  // E1 的 value 被修改了
                    r2vEdge.removeIcmId(icmId);
                    r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
                }
            } else if (isValueE1Modified && r2vEdge.getPort().equals("E1") && r2vEdge.getName().equals(propertyName)
                    && r2vEdge.getEnder().getName().equals(propertyValueE1)) {  // 若新名称已在 CCM 中与该 relationship 连接，则直接利用
                newPropertyValue1AlreadyConnected = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
            }
        }

        // E0
        if (isValueE0Modified && !newPropertyValue0AlreadyConnected) {  // 若新名称没有在 CCM 中与该 relationship 连接
            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, propertyValueE0);
            ValueNode valueNode;
            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                valueNode = valueNodes.get(0);
                valueNode.addIcmId(icmId);
            } else {  // 新名称并不存在于 CCM，则新建
                valueNode = new ValueNode(ccmId, icmId, propertyValueE0);
            }
            RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, "E0", propertyName, relationNode, valueNode);
            relationNode.addR2VEdge(r2vEdge);
            valueNode.addR2VEdge(r2vEdge);
        }
        // E1
        if (isValueE1Modified && !newPropertyValue1AlreadyConnected) {  // 若新名称没有在 CCM 中与该 relationship 连接
            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, propertyValueE1);
            ValueNode valueNode;
            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                valueNode = valueNodes.get(0);
                valueNode.addIcmId(icmId);
            } else {  // 新名称并不存在于 CCM，则新建
                valueNode = new ValueNode(ccmId, icmId, propertyValueE1);
            }
            RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, "E1", propertyName, relationNode, valueNode);
            relationNode.addR2VEdge(r2vEdge);
            valueNode.addR2VEdge(r2vEdge);
        }
    }

    /**
     * 修改某 relationship 节点两端 type 特性的值（注意，E0 和 E1 并不是实际 port，实际上，type 和 name 的 port 都是空字符串）
     * @param ccmId ccmId
     * @param icmId icmId
     * @param relationNode relationship node 提取深度需要大于等于 2
     * @param relationshipType 关系的类型
     * @param relationshipName 关系的名称，可能为空字符串
     */
    private void modifyRelationshipTypeProperty(Long ccmId, Long icmId, RelationNode relationNode, String relationshipType, String relationshipName) {

        String relationshipTypeEdgeName = "is" + relationshipType;
        boolean isRelationshipNameNotNull = !relationshipName.equals("");

        boolean isValueE0Modified = true;  // 假设 E0 端的值的确被修改了
        boolean isValueE1Modified = true;  // 假设 E1 端的值的确被修改了
        boolean newPropertyValue0AlreadyConnected = false;  // 标志位，表示新属性名的 value 点是否已经在 CCM 中与 relationship 点连接
        boolean newPropertyValue1AlreadyConnected = false;  // 标志位，表示新属性名的 value 点是否已经在 CCM 中与 relationship 点连接

        for (RelationToValueEdge r2vEdge: relationNode.getRtvEdges()) {
            // type
            if (isValueE0Modified && r2vEdge.getIcmSet().contains(icmId) && r2vEdge.getPort().equals("")
                    && !r2vEdge.getName().equals("name") && r2vEdge.getEnder().getName().equals("#true")) {  // port 为空的边，其 name 除了“name”就是“isXXXX”了
                if (r2vEdge.getName().equals(relationshipTypeEdgeName)) {  // type 的 edge name 没有修改
                    isValueE0Modified = false;
                } else {  // type 的 edge name 被修改了
                    r2vEdge.removeIcmId(icmId);
                    r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
                }
            } else if (isValueE0Modified && r2vEdge.getPort().equals("") && r2vEdge.getName().equals(relationshipTypeEdgeName)
                    && r2vEdge.getEnder().getName().equals("#true")) {  // 若新名称已在 CCM 中与该 relationship 连接，则直接利用
                newPropertyValue0AlreadyConnected = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
            }
            // name
            if (isValueE1Modified && r2vEdge.getIcmSet().contains(icmId) && r2vEdge.getPort().equals("") && r2vEdge.getName().equals("name")) {  // 删除旧名，考虑了 relationship 没有 name 的情况
                if (r2vEdge.getEnder().getName().equals(relationshipName)) {  // name 的 value 没有修改
                    isValueE1Modified = false;
                } else {  // name 的 value 被修改了
                    r2vEdge.removeIcmId(icmId);
                    r2vEdge.getEnder().removeIcmIdIfNoEdgeAttachedInIcm(icmId);
                }
            } else if (isRelationshipNameNotNull && isValueE1Modified && r2vEdge.getPort().equals("") && r2vEdge.getName().equals("name")
                    && r2vEdge.getEnder().getName().equals(relationshipName)) {  // 若新名称已在 CCM 中与该 relationship 连接，则直接利用（若新名称为空，则不必新加入节点到 ICM）
                newPropertyValue1AlreadyConnected = true;
                r2vEdge.addIcmId(icmId);
                r2vEdge.getEnder().addIcmId(icmId);
            }
        }

        // type
        if (isValueE0Modified && !newPropertyValue0AlreadyConnected) {  // 若新名称没有在 CCM 中与该 relationship 连接
            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, "#true");
            ValueNode valueNode;
            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                valueNode = valueNodes.get(0);
                valueNode.addIcmId(icmId);
            } else {  // 新名称并不存在于 CCM，则新建
                valueNode = new ValueNode(ccmId, icmId, "#true");
            }
            RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, "", relationshipTypeEdgeName, relationNode, valueNode);
            relationNode.addR2VEdge(r2vEdge);
            valueNode.addR2VEdge(r2vEdge);
        }
        // name
        if (isRelationshipNameNotNull && isValueE1Modified && !newPropertyValue1AlreadyConnected) {  // 若新名称没有在 CCM 中与该 relationship 连接（若新名称为空，则不必新加入节点到 ICM）
            List<ValueNode> valueNodes = valueNodeRepository.getByCcmIdAndName(ccmId, relationshipName);
            ValueNode valueNode;
            if (!valueNodes.isEmpty()) {  // 新名称已经存在于 CCM（只是没有与该类连接），则利用
                valueNode = valueNodes.get(0);
                valueNode.addIcmId(icmId);
            } else {  // 新名称并不存在于 CCM，则新建
                valueNode = new ValueNode(ccmId, icmId, relationshipName);
            }
            RelationToValueEdge r2vEdge = new RelationToValueEdge(ccmId, icmId, "", "name", relationNode, valueNode);
            relationNode.addR2VEdge(r2vEdge);
            valueNode.addR2VEdge(r2vEdge);
        }
    }

    /**
     * 修改某 relationship 节点两端 class 特性的值
     * @param ccmId ccmId
     * @param icmId icmId
     * @param relationNode relationship node 提取深度需要大于等于 2
     * @param className0 E0 端的类名
     * @param className1 E1 端的类名
     */
    private void modifyRelationshipClassProperty(Long ccmId, Long icmId, RelationNode relationNode, String className0, String className1) {

        RelationToClassEdge r2cEdge0 = null;
        RelationToClassEdge r2cEdge1 = null;

        // 找到两端的类节点
        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            if (r2cEdge.getIcmSet().contains(icmId) && r2cEdge.getName().equals("class")) {
                if (r2cEdge.getPort().equals("E0")) {
                    r2cEdge0 = r2cEdge;
                } else {
                    r2cEdge1 = r2cEdge;
                }
            }
        }
        assert r2cEdge0 != null && r2cEdge1 != null;

        for (ClassToValueEdge c2vEdge : r2cEdge0.getEnder().getCtvEdges()) {
            if (c2vEdge.getIcmSet().contains(icmId) && c2vEdge.getEnder().getName().equals(className0)) {
                return;  // 0对0，实际并没有交换两端的类，因此直接退出
            }
        }

        // 能执行到此处，说明的确需要交换两端的类
        r2cEdge0.removeIcmId(icmId);
        r2cEdge1.removeIcmId(icmId);
        ClassNode classNode0 = r2cEdge0.getEnder();
        ClassNode classNode1 = r2cEdge1.getEnder();
        classNode0.setIsSettled(false);  // 有待融合算法进一步处理
        classNode1.setIsSettled(false);  // 有待融合算法进一步处理
        boolean e0AlreadyInCcm = false;
        boolean e1AlreadyInCcm = false;

        for (RelationToClassEdge r2cEdge : relationNode.getRtcEdges()) {
            if (r2cEdge.getName().equals("class") && r2cEdge.getPort().equals("E0")
                    && r2cEdge.getEnder() == classNode1) {  // CCM 中有指向原 E1 类的 E0.class 边，可利用
                e0AlreadyInCcm = true;
                r2cEdge.addIcmId(icmId);
            }
            if (r2cEdge.getName().equals("class") && r2cEdge.getPort().equals("E1")
                    && r2cEdge.getEnder() == classNode0) {  // CCM 中有指向原 E0 类的 E1.class 边，可利用
                e1AlreadyInCcm = true;
                r2cEdge.addIcmId(icmId);
            }
        }
        if (!e0AlreadyInCcm) {  // CCM 中没有指向原 E1 类的 E0.class 边，需新建
            RelationToClassEdge r2cEdge = new RelationToClassEdge(ccmId, icmId, "E0", "class", relationNode, classNode1);
            relationNode.addR2CEdge(r2cEdge);
            classNode1.addR2CEdge(r2cEdge);
        }
        if (!e1AlreadyInCcm) {  // CCM 中没有指向原 E0 类的 E1.class 边，需新建
            RelationToClassEdge r2cEdge = new RelationToClassEdge(ccmId, icmId, "E1", "class", relationNode, classNode0);
            relationNode.addR2CEdge(r2cEdge);
            classNode0.addR2CEdge(r2cEdge);
        }
    }
}
