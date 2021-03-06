/*
 * Copyright 2014-2016, Stigmergic-Modeling Project
 * SEIDR, Peking University
 * All rights reserved
 *
 * Stigmergic-Modeling is used for collaborative groups to create a conceptual model.
 * It is based on UML 2.0 class diagram specifications and stigmergy theory.
 */

package net.stigmod.service;

import net.stigmod.domain.conceptualmodel.*;
import net.stigmod.service.migrateService.MigrateHandlerImpl;
import org.junit.Test;

import java.util.HashSet;
import java.util.*;

/**
 * @author Kai Fu
 * @version 2015/12/28
 */


public class MigrateHandlerImplTests {

    List<ClassNode> cLassNodeList=new ArrayList<>();
    List<RelationNode> relationNodeList=new ArrayList<>();
    List<ValueNode> valueNodeList=new ArrayList<>();

    //这些nodeNum记录了对应节点数目
    int vNodeNum=37;
    int cNodeNum=8;
    int rNodeNum=25;//暂定

    int c=0;
    int PersonNum;

    private void cNodeInit() {

        for(long t=0;t<PersonNum;t++) {
            for(int i=0;i<cNodeNum;i++) {
                c++;
                Set<Long> s1=new HashSet<>();
                s1.add(t);
//                ClassNode cNode = classNodeList.get(6*(int)t+i);
                ClassNode cNode = new ClassNode();
                cNode.setIcmSet(s1);
                cNode.setCcmId(0l);
                cNode.setId((long)c);
                cLassNodeList.add(cNode);
            }
        }
    }

    private void rNodeInit() {

        for(long t=0;t<PersonNum;t++) {
            for(int i=0;i<rNodeNum;i++) {
                c++;
                Set<Long> s1=new HashSet<>();
                s1.add(t);
//                RelationNode rNode = relationNodeList.get(7*(int)t+i);
                RelationNode rNode = new RelationNode();
                rNode.setIcmSet(s1);
                rNode.setCcmId(0l);
                rNode.setId((long)c);
                relationNodeList.add(rNode);
            }
        }
    }

    private void vNodeInit() {
        Set<Long> s1=new HashSet<>();
        for(long i=0;i<(long)PersonNum;i++) {
            s1.add(i);
        }

        c++;
        for(int i=0;i < vNodeNum;i++) {
            ValueNode vNode = new ValueNode();
            vNode.setIcmSet(new HashSet<Long>(s1));
            vNode.setCcmId(0l);
            vNode.setId((long)c++);
            valueNodeList.add(vNode);
        }

        //
        valueNodeList.get(0).setName("Person");
        valueNodeList.get(1).setName("Student");
        valueNodeList.get(2).setName("Teacher");
        valueNodeList.get(3).setName("Course");
        valueNodeList.get(4).setName("CourseSchedule");
        valueNodeList.get(5).setName("Integer");
        valueNodeList.get(6).setName("String");
        valueNodeList.get(7).setName("Boolean");

        //
        valueNodeList.get(8).setName("person");
        valueNodeList.get(9).setName("student");
        valueNodeList.get(10).setName("teacher");
        valueNodeList.get(11).setName("course");
        valueNodeList.get(12).setName("courseSchedule");

        //Person attribute
        valueNodeList.get(13).setName("uid");
        valueNodeList.get(14).setName("name");
        valueNodeList.get(15).setName("age");
        valueNodeList.get(16).setName("gender");
        valueNodeList.get(17).setName("email");
        valueNodeList.get(18).setName("tel");

        //Student
        valueNodeList.get(19).setName("sid");
        valueNodeList.get(20).setName("grade");
        valueNodeList.get(21).setName("major");

        //Course
        valueNodeList.get(22).setName("cid");
        //***16-name
        valueNodeList.get(23).setName("credit");
        valueNodeList.get(24).setName("teacherId");

        //Teacher
        valueNodeList.get(25).setName("tid");
        valueNodeList.get(26).setName("title");
        valueNodeList.get(27).setName("hightestEducation");
        valueNodeList.get(28).setName("department");

        //CourseSchedule
        //***15-cid
        valueNodeList.get(29).setName("locate");
        valueNodeList.get(30).setName("date");


        //relation-name
        valueNodeList.get(31).setName("choose");
        valueNodeList.get(32).setName("teaching");
        valueNodeList.get(33).setName("with");

        valueNodeList.get(34).setName("true");

        valueNodeList.get(35).setName("1");
        valueNodeList.get(36).setName("*");
    }

    private void edgeInit() {
        for(long i=0;i<PersonNum;i++) {
            c++;

//            System.out.println("c的值为: "+c);

            Set<Long> s1=new HashSet<>();
            s1.add(i);

            int curI=(int)i;

            //先把所有classNode的指向valueNode的边确定下来
            //第一个是Person
            ClassToValueEdge ctvEdge1 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum),valueNodeList.get(0));
            ctvEdge1.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge1.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum).getCtvEdges().add(ctvEdge1);
            valueNodeList.get(0).getCtvEdges().add(ctvEdge1);
            //第二个是Student
            ClassToValueEdge ctvEdge2 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+1),valueNodeList.get(1));
            ctvEdge2.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge2.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+1).getCtvEdges().add(ctvEdge2);
            valueNodeList.get(1).getCtvEdges().add(ctvEdge2);
            //第三个是Teacher
            ClassToValueEdge ctvEdge3 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+2),valueNodeList.get(2));
            ctvEdge3.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge3.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+2).getCtvEdges().add(ctvEdge3);
            valueNodeList.get(2).getCtvEdges().add(ctvEdge3);
            //第四个是Course
            ClassToValueEdge ctvEdge4 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+3),valueNodeList.get(3));
            ctvEdge4.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge4.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+3).getCtvEdges().add(ctvEdge4);
            valueNodeList.get(3).getCtvEdges().add(ctvEdge4);
            //第五个是CourseSchedule
            ClassToValueEdge ctvEdge5 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+4),valueNodeList.get(4));
            ctvEdge5.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge5.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+4).getCtvEdges().add(ctvEdge5);
            valueNodeList.get(4).getCtvEdges().add(ctvEdge5);
            //第六个是Integer
            ClassToValueEdge ctvEdge6 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+5),valueNodeList.get(5));
            ctvEdge6.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge6.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+5).getCtvEdges().add(ctvEdge6);
            valueNodeList.get(5).getCtvEdges().add(ctvEdge6);
            //第七个是String
            ClassToValueEdge ctvEdge7 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+6),valueNodeList.get(6));
            ctvEdge7.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge7.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+6).getCtvEdges().add(ctvEdge7);
            valueNodeList.get(6).getCtvEdges().add(ctvEdge7);
            //第八个是Boolean
            ClassToValueEdge ctvEdge8 =
                    new ClassToValueEdge("name",cLassNodeList.get(curI*cNodeNum+7),valueNodeList.get(7));
            ctvEdge8.setIcmSet(new HashSet<Long>(s1));
//            ctvEdge8.setLoc(c++);
            cLassNodeList.get(curI*cNodeNum+7).getCtvEdges().add(ctvEdge8);
            valueNodeList.get(7).getCtvEdges().add(ctvEdge8);
            //classNode 一共有8个

            //下面设置Rtv与Rtc节点
            //第一个是关系是Person与Student的关系,泛化关系,以e0为父端,e1为子端
            RelationToClassEdge rtcEdge1 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum),cLassNodeList.get(curI*cNodeNum));
            rtcEdge1.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge1.setLoc(c++);
            relationNodeList.get(curI*rNodeNum).getRtcEdges().add(rtcEdge1);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge1);

            RelationToValueEdge rtvEdge1 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum),valueNodeList.get(8));
            rtvEdge1.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge1.setLoc(c++);
            relationNodeList.get(curI*rNodeNum).getRtvEdges().add(rtvEdge1);
            valueNodeList.get(8).getRtvEdges().add(rtvEdge1);

            RelationToClassEdge rtcEdge2 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum),cLassNodeList.get(curI*cNodeNum+1));
            rtcEdge2.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge2.setLoc(c++);
            relationNodeList.get(curI*rNodeNum).getRtcEdges().add(rtcEdge2);
            cLassNodeList.get(curI*cNodeNum+1).getRtcEdges().add(rtcEdge2);

            RelationToValueEdge rtvEdge2 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum),valueNodeList.get(9));
            rtvEdge2.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge2.setLoc(c++);
            relationNodeList.get(curI*rNodeNum).getRtvEdges().add(rtvEdge2);
            valueNodeList.get(9).getRtvEdges().add(rtvEdge2);

            //一条额外的generalization边,表明该关系为泛化的
            RelationToValueEdge rtvEdge3 =
                    new RelationToValueEdge("","isGeneralization",relationNodeList.get(curI*rNodeNum), valueNodeList.get(34));
            rtvEdge3.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge3.setLoc(c++);
            relationNodeList.get(curI*rNodeNum).getRtvEdges().add(rtvEdge3);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge3);

            //第一个关系结束,第二个关系是Person与Teacher的关系

            RelationToClassEdge rtcEdge4 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+1),cLassNodeList.get(curI*cNodeNum));
            rtcEdge4.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge4.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+1).getRtcEdges().add(rtcEdge4);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge4);

            RelationToValueEdge rtvEdge5 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+1),valueNodeList.get(8));
            rtvEdge5.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge5.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+1).getRtvEdges().add(rtvEdge5);
            valueNodeList.get(8).getRtvEdges().add(rtvEdge5);

            RelationToClassEdge rtcEdge6 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+1),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge6.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge6.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+1).getRtcEdges().add(rtcEdge6);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge6);

            RelationToValueEdge rtvEdge7 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+1),valueNodeList.get(10));
            rtvEdge7.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge7.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+1).getRtvEdges().add(rtvEdge7);
            valueNodeList.get(10).getRtvEdges().add(rtvEdge7);

            //一条额外的generalization边,表明该关系为泛化的
            RelationToValueEdge rtvEdge8 =
                    new RelationToValueEdge("","isGeneralization",relationNodeList.get(curI*rNodeNum+1), valueNodeList.get(34));
            rtvEdge8.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge8.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+1).getRtvEdges().add(rtvEdge8);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge8);
            //第二个关系节点建立完毕

            //第三个关系是Student与Course的关系,1对*,关系名为choose
            RelationToClassEdge rtcEdge9 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+2),cLassNodeList.get(curI*cNodeNum+1));
            rtcEdge9.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge9.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtcEdges().add(rtcEdge9);
            cLassNodeList.get(curI*cNodeNum+1).getRtcEdges().add(rtcEdge9);

            RelationToValueEdge rtvEdge10 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(9));
            rtvEdge10.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge10.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge10);
            valueNodeList.get(9).getRtvEdges().add(rtvEdge10);

            RelationToValueEdge rtvEdge11 =
                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge12 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+2),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge12.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge12.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtcEdges().add(rtcEdge12);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge12);

            RelationToValueEdge rtvEdge13 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(11));
            rtvEdge13.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge13.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge13);
            valueNodeList.get(11).getRtvEdges().add(rtvEdge13);

            RelationToValueEdge rtvEdge14 =
                    new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge15 =
                    new RelationToValueEdge("","edge name",relationNodeList.get(curI*rNodeNum+2), valueNodeList.get(31));
            rtvEdge15.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge15.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge15);
            valueNodeList.get(31).getRtvEdges().add(rtvEdge15);

            //下面是第四个关系Course与Teacher,*对1的多重性,边名为teaching
            RelationToClassEdge rtcEdge16 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+3),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge16.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge16.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtcEdges().add(rtcEdge16);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge16);

            RelationToValueEdge rtvEdge17 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+3),valueNodeList.get(11));
            rtvEdge17.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge17.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtvEdges().add(rtvEdge17);
            valueNodeList.get(11).getRtvEdges().add(rtvEdge17);

            RelationToValueEdge rtvEdge18 =//多重性尚未修改
                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+3),valueNodeList.get(36));
            rtvEdge18.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge18.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtvEdges().add(rtvEdge18);
            valueNodeList.get(36).getRtvEdges().add(rtvEdge18);

            RelationToClassEdge rtcEdge19 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+3),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge19.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge19.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtcEdges().add(rtcEdge19);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge19);

            RelationToValueEdge rtvEdge20 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+3),valueNodeList.get(10));
            rtvEdge20.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge20.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtvEdges().add(rtvEdge20);
            valueNodeList.get(10).getRtvEdges().add(rtvEdge20);

            RelationToValueEdge rtvEdge21 = //暂没修改
                    new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+3),valueNodeList.get(35));
            rtvEdge21.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge21.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtvEdges().add(rtvEdge21);
            valueNodeList.get(35).getRtvEdges().add(rtvEdge21);

            RelationToValueEdge rtvEdge22 =
                    new RelationToValueEdge("","edge name",relationNodeList.get(curI*rNodeNum+3), valueNodeList.get(32));
            rtvEdge22.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge22.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+3).getRtvEdges().add(rtvEdge22);
            valueNodeList.get(32).getRtvEdges().add(rtvEdge22);

            //下面建立第5个关系 Course与CourseSchedule
            RelationToClassEdge rtcEdge23 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+4),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge23.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge23.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtcEdges().add(rtcEdge23);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge23);

            RelationToValueEdge rtvEdge24 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+4),valueNodeList.get(11));
            rtvEdge24.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge24.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtvEdges().add(rtvEdge24);
            valueNodeList.get(11).getRtvEdges().add(rtvEdge24);

            RelationToValueEdge rtvEdge25 =//多重性尚未修改
                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+4),valueNodeList.get(35));
            rtvEdge25.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge25.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtvEdges().add(rtvEdge25);
            valueNodeList.get(35).getRtvEdges().add(rtvEdge25);

            RelationToClassEdge rtcEdge26 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+4),cLassNodeList.get(curI*cNodeNum+4));
            rtcEdge26.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge26.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtcEdges().add(rtcEdge26);
            cLassNodeList.get(curI*cNodeNum+4).getRtcEdges().add(rtcEdge26);

            RelationToValueEdge rtvEdge27 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+4),valueNodeList.get(12));
            rtvEdge27.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge27.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtvEdges().add(rtvEdge27);
            valueNodeList.get(12).getRtvEdges().add(rtvEdge27);

            RelationToValueEdge rtvEdge28 = //暂没修改
                    new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+4),valueNodeList.get(35));
            rtvEdge28.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge28.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtvEdges().add(rtvEdge28);
            valueNodeList.get(35).getRtvEdges().add(rtvEdge28);

            RelationToValueEdge rtvEdge29 =
                    new RelationToValueEdge("","edge name",relationNodeList.get(curI*rNodeNum+4), valueNodeList.get(33));
            rtvEdge29.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge29.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+4).getRtvEdges().add(rtvEdge29);
            valueNodeList.get(33).getRtvEdges().add(rtvEdge29);

            //完成了基本的五个关系的建立,剩下20个关系则是属性关系了
            //第一个是Person与uid的
            RelationToClassEdge rtcEdge30 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+5),cLassNodeList.get(curI*cNodeNum));
            rtcEdge30.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge30.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+5).getRtcEdges().add(rtcEdge30);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge30);

            RelationToValueEdge rtvEdge31 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+5),valueNodeList.get(0));
            rtvEdge31.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge31.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+5).getRtvEdges().add(rtvEdge31);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge31);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge33 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+5),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge33.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge33.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+5).getRtcEdges().add(rtcEdge33);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge33);

            RelationToValueEdge rtvEdge34 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+5),valueNodeList.get(13));
            rtvEdge34.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge34.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+5).getRtvEdges().add(rtvEdge34);
            valueNodeList.get(13).getRtvEdges().add(rtvEdge34);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge36 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+5), valueNodeList.get(34));
            rtvEdge36.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge36.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+5).getRtvEdges().add(rtvEdge36);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge36);

            //Person与name的属性
            RelationToClassEdge rtcEdge37 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+6),cLassNodeList.get(curI*cNodeNum));
            rtcEdge37.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge37.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+6).getRtcEdges().add(rtcEdge37);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge37);

            RelationToValueEdge rtvEdge38 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+6),valueNodeList.get(0));
            rtvEdge38.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge38.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+6).getRtvEdges().add(rtvEdge38);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge38);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge40 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+6),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge40.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge40.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+6).getRtcEdges().add(rtcEdge40);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge40);

            RelationToValueEdge rtvEdge41 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+6),valueNodeList.get(14));
            rtvEdge41.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge41.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+6).getRtvEdges().add(rtvEdge41);
            valueNodeList.get(14).getRtvEdges().add(rtvEdge41);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge43 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+6), valueNodeList.get(34));
            rtvEdge43.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge43.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+6).getRtvEdges().add(rtvEdge43);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge43);

            //下一个Person与age
            RelationToClassEdge rtcEdge44 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+7),cLassNodeList.get(curI*cNodeNum));
            rtcEdge44.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge44.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+7).getRtcEdges().add(rtcEdge44);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge44);

            RelationToValueEdge rtvEdge45 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+7),valueNodeList.get(0));
            rtvEdge45.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge45.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+7).getRtvEdges().add(rtvEdge45);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge45);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge47 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+7),cLassNodeList.get(curI*cNodeNum+5));
            rtcEdge47.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge47.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+7).getRtcEdges().add(rtcEdge47);
            cLassNodeList.get(curI*cNodeNum+5).getRtcEdges().add(rtcEdge47);

            RelationToValueEdge rtvEdge48 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+7),valueNodeList.get(15));
            rtvEdge48.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge48.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+7).getRtvEdges().add(rtvEdge48);
            valueNodeList.get(15).getRtvEdges().add(rtvEdge48);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge50 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+7), valueNodeList.get(34));
            rtvEdge50.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge50.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+7).getRtvEdges().add(rtvEdge50);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge50);

            //下一个是Perosn与gender
            RelationToClassEdge rtcEdge51 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+8),cLassNodeList.get(curI*cNodeNum));
            rtcEdge51.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge51.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+8).getRtcEdges().add(rtcEdge51);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge51);

            RelationToValueEdge rtvEdge52 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+8),valueNodeList.get(0));
            rtvEdge52.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge52.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+8).getRtvEdges().add(rtvEdge52);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge52);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge54 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+8),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge54.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge54.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+8).getRtcEdges().add(rtcEdge54);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge54);

            RelationToValueEdge rtvEdge55 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+8),valueNodeList.get(16));
            rtvEdge55.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge55.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+8).getRtvEdges().add(rtvEdge55);
            valueNodeList.get(16).getRtvEdges().add(rtvEdge55);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge57 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+8), valueNodeList.get(34));
            rtvEdge57.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge57.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+8).getRtvEdges().add(rtvEdge57);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge57);

            //Person与email的关系
            RelationToClassEdge rtcEdge58 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+9),cLassNodeList.get(curI*cNodeNum));
            rtcEdge58.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge58.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+9).getRtcEdges().add(rtcEdge58);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge58);

            RelationToValueEdge rtvEdge59 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+9),valueNodeList.get(0));
            rtvEdge59.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge59.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+9).getRtvEdges().add(rtvEdge59);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge59);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge61 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+9),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge61.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge61.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+9).getRtcEdges().add(rtcEdge61);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge61);

            RelationToValueEdge rtvEdge62 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+9),valueNodeList.get(17));
            rtvEdge62.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge62.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+9).getRtvEdges().add(rtvEdge62);
            valueNodeList.get(17).getRtvEdges().add(rtvEdge62);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge64 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+9), valueNodeList.get(34));
            rtvEdge64.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge64.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+9).getRtvEdges().add(rtvEdge64);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge64);

            //下一个是Person与tel的关系
            RelationToClassEdge rtcEdge65 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+10),cLassNodeList.get(curI*cNodeNum));
            rtcEdge65.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge65.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+10).getRtcEdges().add(rtcEdge65);
            cLassNodeList.get(curI*cNodeNum).getRtcEdges().add(rtcEdge65);

            RelationToValueEdge rtvEdge66 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+10),valueNodeList.get(0));
            rtvEdge66.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge66.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+10).getRtvEdges().add(rtvEdge66);
            valueNodeList.get(0).getRtvEdges().add(rtvEdge66);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge68 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+10),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge68.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge68.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+10).getRtcEdges().add(rtcEdge68);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge68);

            RelationToValueEdge rtvEdge69 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+10),valueNodeList.get(18));
            rtvEdge69.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge69.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+10).getRtvEdges().add(rtvEdge69);
            valueNodeList.get(18).getRtvEdges().add(rtvEdge69);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge71 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+10), valueNodeList.get(34));
            rtvEdge71.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge71.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+10).getRtvEdges().add(rtvEdge71);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge71);

            //Student与sid的属性关系
            RelationToClassEdge rtcEdge72 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+11),cLassNodeList.get(curI*cNodeNum+1));
            rtcEdge72.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge72.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+11).getRtcEdges().add(rtcEdge72);
            cLassNodeList.get(curI*cNodeNum+1).getRtcEdges().add(rtcEdge72);

            RelationToValueEdge rtvEdge73 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+11),valueNodeList.get(1));
            rtvEdge73.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge73.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+11).getRtvEdges().add(rtvEdge73);
            valueNodeList.get(1).getRtvEdges().add(rtvEdge73);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge75 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+11),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge75.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge75.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+11).getRtcEdges().add(rtcEdge75);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge75);

            RelationToValueEdge rtvEdge76 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+11),valueNodeList.get(19));
            rtvEdge76.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge76.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+11).getRtvEdges().add(rtvEdge76);
            valueNodeList.get(19).getRtvEdges().add(rtvEdge76);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge78 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+11), valueNodeList.get(34));
            rtvEdge78.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge78.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+11).getRtvEdges().add(rtvEdge78);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge78);

            //下一个是Student与grade
            RelationToClassEdge rtcEdge79 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+12),cLassNodeList.get(curI*cNodeNum+1));
            rtcEdge79.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge79.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+12).getRtcEdges().add(rtcEdge79);
            cLassNodeList.get(curI*cNodeNum+1).getRtcEdges().add(rtcEdge79);

            RelationToValueEdge rtvEdge80 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+12),valueNodeList.get(1));
            rtvEdge80.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge80.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+12).getRtvEdges().add(rtvEdge80);
            valueNodeList.get(1).getRtvEdges().add(rtvEdge80);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge82 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+12),cLassNodeList.get(curI*cNodeNum+5));
            rtcEdge82.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge82.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+12).getRtcEdges().add(rtcEdge82);
            cLassNodeList.get(curI*cNodeNum+5).getRtcEdges().add(rtcEdge82);

            RelationToValueEdge rtvEdge83 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+12),valueNodeList.get(20));
            rtvEdge83.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge83.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+12).getRtvEdges().add(rtvEdge83);
            valueNodeList.get(20).getRtvEdges().add(rtvEdge83);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge85 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+12), valueNodeList.get(34));
            rtvEdge85.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge85.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+12).getRtvEdges().add(rtvEdge85);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge85);

            //Student与major的属性关系
            RelationToClassEdge rtcEdge86 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+13),cLassNodeList.get(curI*cNodeNum+1));
            rtcEdge86.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge86.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+13).getRtcEdges().add(rtcEdge86);
            cLassNodeList.get(curI*cNodeNum+1).getRtcEdges().add(rtcEdge86);

            RelationToValueEdge rtvEdge87 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+13),valueNodeList.get(1));
            rtvEdge87.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge87.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+13).getRtvEdges().add(rtvEdge87);
            valueNodeList.get(1).getRtvEdges().add(rtvEdge87);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge89 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+13),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge89.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge89.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+13).getRtcEdges().add(rtcEdge89);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge89);

            RelationToValueEdge rtvEdge90 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+13),valueNodeList.get(21));
            rtvEdge90.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge90.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+13).getRtvEdges().add(rtvEdge90);
            valueNodeList.get(21).getRtvEdges().add(rtvEdge90);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge92 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+13), valueNodeList.get(34));
            rtvEdge92.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge92.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+13).getRtvEdges().add(rtvEdge92);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge92);

            //下面是Course的相关属性
            //首先是Course和cid的
            RelationToClassEdge rtcEdge93 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+14),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge93.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge93.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+14).getRtcEdges().add(rtcEdge93);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge93);

            RelationToValueEdge rtvEdge94 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+14),valueNodeList.get(3));
            rtvEdge94.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge94.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+14).getRtvEdges().add(rtvEdge94);
            valueNodeList.get(3).getRtvEdges().add(rtvEdge94);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge96 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+14),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge96.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge96.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+14).getRtcEdges().add(rtcEdge96);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge96);

            RelationToValueEdge rtvEdge97 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+14),valueNodeList.get(22));
            rtvEdge97.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge97.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+14).getRtvEdges().add(rtvEdge97);
            valueNodeList.get(22).getRtvEdges().add(rtvEdge97);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge99 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+14), valueNodeList.get(34));
            rtvEdge99.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge99.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+14).getRtvEdges().add(rtvEdge99);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge99);

            //其次是Course和name的
            RelationToClassEdge rtcEdge100 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+15),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge100.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge100.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+15).getRtcEdges().add(rtcEdge100);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge100);

            RelationToValueEdge rtvEdge101 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+15),valueNodeList.get(3));
            rtvEdge101.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge101.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+15).getRtvEdges().add(rtvEdge101);
            valueNodeList.get(3).getRtvEdges().add(rtvEdge101);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge103 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+15),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge103.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge103.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+15).getRtcEdges().add(rtcEdge103);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge103);

            RelationToValueEdge rtvEdge104 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+15),valueNodeList.get(14));
            rtvEdge104.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge104.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+15).getRtvEdges().add(rtvEdge104);
            valueNodeList.get(14).getRtvEdges().add(rtvEdge104);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge106 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+15), valueNodeList.get(34));
            rtvEdge106.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge106.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+15).getRtvEdges().add(rtvEdge106);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge106);

            //Course和credit
            RelationToClassEdge rtcEdge107 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+16),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge107.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge107.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+16).getRtcEdges().add(rtcEdge107);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge107);

            RelationToValueEdge rtvEdge108 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+16),valueNodeList.get(3));
            rtvEdge108.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge108.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+16).getRtvEdges().add(rtvEdge108);
            valueNodeList.get(3).getRtvEdges().add(rtvEdge108);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge110 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+16),cLassNodeList.get(curI*cNodeNum+5));
            rtcEdge110.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge110.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+16).getRtcEdges().add(rtcEdge110);
            cLassNodeList.get(curI*cNodeNum+5).getRtcEdges().add(rtcEdge110);

            RelationToValueEdge rtvEdge111 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+16),valueNodeList.get(23));
            rtvEdge111.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge111.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+16).getRtvEdges().add(rtvEdge111);
            valueNodeList.get(23).getRtvEdges().add(rtvEdge111);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge113 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+16), valueNodeList.get(34));
            rtvEdge113.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge113.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+16).getRtvEdges().add(rtvEdge113);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge113);

            //Course与teacherId
            RelationToClassEdge rtcEdge114 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+17),cLassNodeList.get(curI*cNodeNum+3));
            rtcEdge114.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge114.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+17).getRtcEdges().add(rtcEdge114);
            cLassNodeList.get(curI*cNodeNum+3).getRtcEdges().add(rtcEdge114);

            RelationToValueEdge rtvEdge115 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+17),valueNodeList.get(3));
            rtvEdge115.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge115.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+17).getRtvEdges().add(rtvEdge115);
            valueNodeList.get(3).getRtvEdges().add(rtvEdge115);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge117 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+17),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge117.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge117.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+17).getRtcEdges().add(rtcEdge117);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge117);

            RelationToValueEdge rtvEdge118 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+17),valueNodeList.get(24));
            rtvEdge118.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge118.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+17).getRtvEdges().add(rtvEdge118);
            valueNodeList.get(24).getRtvEdges().add(rtvEdge118);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge120 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+17), valueNodeList.get(34));
            rtvEdge120.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge120.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+17).getRtvEdges().add(rtvEdge120);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge120);

            //接下来是Teacher的属性关系
            //首先是Teacher与tid
            RelationToClassEdge rtcEdge121 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+18),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge121.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge121.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+18).getRtcEdges().add(rtcEdge121);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge121);

            RelationToValueEdge rtvEdge122 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+18),valueNodeList.get(2));
            rtvEdge122.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge122.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+18).getRtvEdges().add(rtvEdge122);
            valueNodeList.get(2).getRtvEdges().add(rtvEdge122);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge124 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+18),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge124.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge124.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+18).getRtcEdges().add(rtcEdge124);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge124);

            RelationToValueEdge rtvEdge125 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+18),valueNodeList.get(25));
            rtvEdge125.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge125.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+18).getRtvEdges().add(rtvEdge125);
            valueNodeList.get(25).getRtvEdges().add(rtvEdge125);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge127 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+18), valueNodeList.get(34));
            rtvEdge127.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge127.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+18).getRtvEdges().add(rtvEdge127);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge127);

            //Teacher与title的关系
            RelationToClassEdge rtcEdge128 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+19),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge128.setIcmSet(new HashSet<Long>(s1));
//            rtcE/dge128.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+19).getRtcEdges().add(rtcEdge128);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge128);

            RelationToValueEdge rtvEdge129 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+19),valueNodeList.get(2));
            rtvEdge129.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge129.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+19).getRtvEdges().add(rtvEdge129);
            valueNodeList.get(2).getRtvEdges().add(rtvEdge129);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge131 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+19),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge131.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge131.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+19).getRtcEdges().add(rtcEdge131);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge131);

            RelationToValueEdge rtvEdge132 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+19),valueNodeList.get(26));
            rtvEdge132.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge132.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+19).getRtvEdges().add(rtvEdge132);
            valueNodeList.get(26).getRtvEdges().add(rtvEdge132);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge134 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+19), valueNodeList.get(34));
            rtvEdge134.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge134.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+19).getRtvEdges().add(rtvEdge134);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge134);

            //下面是Teacher与hightestEducation
            RelationToClassEdge rtcEdge135 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+20),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge135.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge135.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+20).getRtcEdges().add(rtcEdge135);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge135);

            RelationToValueEdge rtvEdge136 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+20),valueNodeList.get(2));
            rtvEdge136.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge136.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+20).getRtvEdges().add(rtvEdge136);
            valueNodeList.get(2).getRtvEdges().add(rtvEdge136);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge138 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+20),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge138.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge138.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+20).getRtcEdges().add(rtcEdge138);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge138);

            RelationToValueEdge rtvEdge139 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+20),valueNodeList.get(27));
            rtvEdge139.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge139.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+20).getRtvEdges().add(rtvEdge139);
            valueNodeList.get(27).getRtvEdges().add(rtvEdge139);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge141 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+20), valueNodeList.get(34));
            rtvEdge141.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge141.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+20).getRtvEdges().add(rtvEdge141);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge141);

            //下面是Teacher与department
            RelationToClassEdge rtcEdge142 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+21),cLassNodeList.get(curI*cNodeNum+2));
            rtcEdge142.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge142.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+21).getRtcEdges().add(rtcEdge142);
            cLassNodeList.get(curI*cNodeNum+2).getRtcEdges().add(rtcEdge142);

            RelationToValueEdge rtvEdge143 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+21),valueNodeList.get(2));
            rtvEdge143.setIcmSet(new HashSet<Long>(s1));
//            rtvEd/ge143.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+21).getRtvEdges().add(rtvEdge143);
            valueNodeList.get(2).getRtvEdges().add(rtvEdge143);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge145 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+21),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge145.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge145.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+21).getRtcEdges().add(rtcEdge145);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge145);

            RelationToValueEdge rtvEdge146 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+21),valueNodeList.get(28));
            rtvEdge146.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge146.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+21).getRtvEdges().add(rtvEdge146);
            valueNodeList.get(28).getRtvEdges().add(rtvEdge146);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge148 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+21), valueNodeList.get(34));
            rtvEdge148.setIcmSet(new HashSet<Long>(s1));
//            rtvEdg/e148.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+21).getRtvEdges().add(rtvEdge148);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge148);

            //接下来是CourseSchedule的属性关系
            RelationToClassEdge rtcEdge149 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+22),cLassNodeList.get(curI*cNodeNum+4));
            rtcEdge149.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge149.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+22).getRtcEdges().add(rtcEdge149);
            cLassNodeList.get(curI*cNodeNum+4).getRtcEdges().add(rtcEdge149);

            RelationToValueEdge rtvEdge150 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+22),valueNodeList.get(4));
            rtvEdge150.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge150.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+22).getRtvEdges().add(rtvEdge150);
            valueNodeList.get(4).getRtvEdges().add(rtvEdge150);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge152 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+22),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge152.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge152.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+22).getRtcEdges().add(rtcEdge152);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge152);

            RelationToValueEdge rtvEdge153 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+22),valueNodeList.get(22));
            rtvEdge153.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge153.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+22).getRtvEdges().add(rtvEdge153);
            valueNodeList.get(22).getRtvEdges().add(rtvEdge153);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge155 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+22), valueNodeList.get(34));
            rtvEdge155.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge155.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+22).getRtvEdges().add(rtvEdge155);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge155);

            //下面是CourseSchedule与locate
            RelationToClassEdge rtcEdge156 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+23),cLassNodeList.get(curI*cNodeNum+4));
            rtcEdge156.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge156.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+23).getRtcEdges().add(rtcEdge156);
            cLassNodeList.get(curI*cNodeNum+4).getRtcEdges().add(rtcEdge156);

            RelationToValueEdge rtvEdge157 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+23),valueNodeList.get(4));
            rtvEdge157.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge157.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+23).getRtvEdges().add(rtvEdge157);
            valueNodeList.get(4).getRtvEdges().add(rtvEdge157);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge159 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+23),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge159.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge159.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+23).getRtcEdges().add(rtcEdge159);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge159);

            RelationToValueEdge rtvEdge160 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+23),valueNodeList.get(29));
            rtvEdge160.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge160.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+23).getRtvEdges().add(rtvEdge160);
            valueNodeList.get(29).getRtvEdges().add(rtvEdge160);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge162 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+23), valueNodeList.get(34));
            rtvEdge162.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge162.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+23).getRtvEdges().add(rtvEdge162);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge162);

            //下面是CourseSchedule与date
            RelationToClassEdge rtcEdge163 =
                    new RelationToClassEdge("e0","class",relationNodeList.get(curI*rNodeNum+24),cLassNodeList.get(curI*cNodeNum+4));
            rtcEdge163.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge163.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+24).getRtcEdges().add(rtcEdge163);
            cLassNodeList.get(curI*cNodeNum+4).getRtcEdges().add(rtcEdge163);

            RelationToValueEdge rtvEdge164 =
                    new RelationToValueEdge("e0","role",relationNodeList.get(curI*rNodeNum+24),valueNodeList.get(4));
            rtvEdge164.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge164.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+24).getRtvEdges().add(rtvEdge164);
            valueNodeList.get(4).getRtvEdges().add(rtvEdge164);

//            RelationToValueEdge rtvEdge11 =//多重性尚未修改
//                    new RelationToValueEdge("e0","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(35));
//            rtvEdge11.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge11.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge11);
//            valueNodeList.get(35).getRtvEdges().add(rtvEdge11);

            RelationToClassEdge rtcEdge166 =
                    new RelationToClassEdge("e1","class",relationNodeList.get(curI*rNodeNum+24),cLassNodeList.get(curI*cNodeNum+6));
            rtcEdge166.setIcmSet(new HashSet<Long>(s1));
//            rtcEdge166.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+24).getRtcEdges().add(rtcEdge166);
            cLassNodeList.get(curI*cNodeNum+6).getRtcEdges().add(rtcEdge166);

            RelationToValueEdge rtvEdge167 =
                    new RelationToValueEdge("e1","role",relationNodeList.get(curI*rNodeNum+24),valueNodeList.get(30));
            rtvEdge167.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge167.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+24).getRtvEdges().add(rtvEdge167);
            valueNodeList.get(30).getRtvEdges().add(rtvEdge167);

//            RelationToValueEdge rtvEdge14 = //暂没修改
//                  new RelationToValueEdge("e1","multi",relationNodeList.get(curI*rNodeNum+2),valueNodeList.get(36));
//            rtvEdge14.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge14.setLoc(c++);
//            relationNodeList.get(curI*rNodeNum+2).getRtvEdges().add(rtvEdge14);
//            valueNodeList.get(36).getRtvEdges().add(rtvEdge14);

            RelationToValueEdge rtvEdge169 =
                    new RelationToValueEdge("","isAttribute",relationNodeList.get(curI*rNodeNum+24), valueNodeList.get(34));
            rtvEdge169.setIcmSet(new HashSet<Long>(s1));
//            rtvEdge169.setLoc(c++);
            relationNodeList.get(curI*rNodeNum+24).getRtvEdges().add(rtvEdge169);
            valueNodeList.get(34).getRtvEdges().add(rtvEdge169);


//            System.out.println("当前c值: "+c);
        }

    }

    private void initTest() {
        cNodeInit();
        rNodeInit();
        vNodeInit();
        edgeInit();
    }

    @Test
    public void testMigrate() {
        this.PersonNum=13;
        initTest();
        MigrateHandlerImpl migrateHandler=new MigrateHandlerImpl();
        migrateHandler.migrateInitForTest(cLassNodeList,relationNodeList,valueNodeList,c++);
        migrateHandler.migrateHandler();
    }

//    @Test
//    public void testRandomValue() {
//        this.PersonNum=2;
//        initTest();
//        MigrateHandlerImpl migrateHandler = new MigrateHandlerImpl();
//        migrateHandler.migrateInitForTest(cLassNodeList,relationNodeList,valueNodeList);
//        int[] randList=migrateHandler.randomValue();
//        for(int i=0;i<randList.length;i++) {
//            System.out.print(randList[i]+" ,");
//        }
//        System.out.println();
//    }

//    @Test
//    public void testGetTheUserSetForClassNode() {
//        initTest();
//        ClassNode cNode = classNodeList.get(0);
//        for(ClassToValueEdge ctvEdge : cNode.getCtvEdges()) {
//            ctvEdge.getIcmSet().add(2l);
//            ctvEdge.getIcmSet().add(3l);
//            ctvEdge.getIcmSet().add(5l);
//        }
//        for(RelationToClassEdge rtcEdge : cNode.getRtcEdges()) {
//            rtcEdge.getIcmSet().add(4l);
//            rtcEdge.getIcmSet().add(6l);
//        }
//
//
//        RelationNode rNode = relationNodeList.get(0);
//        int t=0;
//        for(RelationToClassEdge rtcEdge : rNode.getRtcEdges()) {
//            rtcEdge.getIcmSet().add(2l);
//            rtcEdge.getIcmSet().add(3l);
//            rtcEdge.getIcmSet().add(4l);
//            t++;
//            if(t==1) rtcEdge.getIcmSet().add(6l);
//        }
//
//        for(RelationToValueEdge rtvEdge : rNode.getRtvEdges()) {
//            rtvEdge.getIcmSet().add(5l);
//            rtvEdge.getIcmSet().add(6l);
//            rtvEdge.getIcmSet().add(7l);
//        }
//
//        MigrateUtil migrateUtil = new MigrateUtil();
//        migrateUtil.getTheUserSetForRelationNode(relationNodeList.get(0));
//    }


//    @Test
//    public void testMap() {
//        Map<String,List<Set<Long>>> myMap = new HashMap<>();
//        List<Set<Long>> lists=new ArrayList<>();
//        Set<Long> s1=new HashSet<>();
//        Set<Long> s2=new HashSet<>();
//        Set<Long> s3=new HashSet<>();
//        Set<Long> s4=new HashSet<>();
//
//        s1.add(1l);
//        s1.add(2l);
//        s1.add(3l);
//
//        s2.add(1l);
//        s2.add(3l);
//        s2.add(5l);
//
//        s3.add(2l);
//        s3.add(3l);
//        s3.add(4l);
//
//        s4.add(1l);
//        s4.add(5l);
//
//        lists.add(s1);
//        lists.add(s2);
//        lists.add(s3);
//        lists.add(s4);
//
//        myMap.put("name",lists);
//
//        EntropyHandler entropyHandler=new EntropyHandlerImpl();
//        entropyHandler.computeMapEntropy(myMap,5);
//    }
}
