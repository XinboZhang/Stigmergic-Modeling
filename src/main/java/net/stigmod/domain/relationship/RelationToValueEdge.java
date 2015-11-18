/*
 * Copyright 2014-2016, Stigmergic-Modeling Project
 * SEIDR, Peking University
 * All rights reserved
 *
 * Stigmergic-Modeling is used for collaborative groups to create a conceptual model.
 * It is based on UML 2.0 class diagram specifications and stigmergy theory.
 */

package net.stigmod.domain.relationship;

import net.stigmod.domain.node.RelationNode;
import net.stigmod.domain.node.ValueNode;
import org.neo4j.ogm.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Kai Fu
 * @version 2015/11/10
 */
@RelationshipEntity(type="PROPERTY")
public class RelationToValueEdge {
    @GraphId
    private Long id;

    @StartNode
    private RelationNode starter;

    @EndNode
    private ValueNode ender;

    @Property
    private String port;

    @Property(name="name")
    private String edgeName;

    @Property(name="icm_list")
    private Set<Long> icmList=new HashSet<Long>();

    @Property
    private Long modelId;

    public RelationToValueEdge() {
        this.port="";
    }

    public RelationToValueEdge(String port , String edgeName, RelationNode starter, ValueNode ender) {
        this.port=port;
        this.edgeName = edgeName;
        this.starter = starter;
        this.ender = ender;
    }

    public Long getId() {
        return id;
    }

    public RelationNode getStarter() {
        return starter;
    }

    public void setStarter(RelationNode starter) {
        this.starter = starter;
    }

    public ValueNode getEnder() {
        return ender;
    }

    public void setEnder(ValueNode ender) {
        this.ender = ender;
    }

    public String getEdgeName() {
        return edgeName;
    }

    public Set<Long> getIcmList() {
        return icmList;
    }

    public void setIcmList(Set<Long> icmList) {
        this.icmList = icmList;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Long getModelId() {
        return modelId;
    }

    public void setModelId(Long modelId) {
        this.modelId = modelId;
    }
}