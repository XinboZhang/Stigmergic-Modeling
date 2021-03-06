/*
 * Copyright 2014-2016, Stigmergic-Modeling Project
 * SEIDR, Peking University
 * All rights reserved
 *
 * Stigmergic-Modeling is used for collaborative groups to create a conceptual model.
 * It is based on UML 2.0 class diagram specifications and stigmergy theory.
 */

package net.stigmod.domain.conceptualmodel;

import org.neo4j.ogm.annotation.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Kai Fu
 * @author Shijun Wang
 * @version 2015/11/10
 */

@RelationshipEntity(type="E_CLASS")
public class RelationToClassEdge extends AbstractEdge {

    @StartNode
    private RelationNode starter;

    @EndNode
    private ClassNode ender;

    private Long ccmId;
    private Set<Long> icmSetPreCopy = new HashSet<>();
    private boolean isChanged=false;

    public RelationToClassEdge() {
        this.port="";
    }

    public RelationToClassEdge(RelationToClassEdge rtcEdge) {
        this.id=rtcEdge.getId();
        this.starter=new RelationNode(rtcEdge.getStarter());
        this.ender=new ClassNode(rtcEdge.getEnder());
        this.port=rtcEdge.port;
        this.name =rtcEdge.getName();
        this.setIcmSet(rtcEdge.getIcmSet());
        this.ccmId =rtcEdge.getCcmId();
        this.updateDisplayName();
    }

    public RelationToClassEdge(String port, String name, RelationNode starter, ClassNode ender) {
        super();
        this.name = name;
        this.starter=starter;
        this.ender=ender;
        this.port=port;
        this.updateDisplayName();
    }

    public RelationToClassEdge(Long ccmId, Long icmId, String port, RelationNode starter, ClassNode ender) {
        this(ccmId, icmId, port, "class", starter, ender);
    }

    public RelationToClassEdge(Long ccmId, Long icmId, String port, String name, RelationNode starter, ClassNode ender) {
        super();
        this.ccmId = ccmId;
        this.icmSet.add(icmId.toString());
        this.port = port;
        this.name = name;
        this.starter = starter;
        this.ender = ender;
        this.updateDisplayName();
    }

    public void UpdateRelationToCEdge(RelationToClassEdge relationToClassEdge) {
        this.setIcmSet(relationToClassEdge.getIcmSet());
    }

    public RelationNode getStarter() {
        return starter;
    }

    public ClassNode getEnder() {
        return ender;
    }

    public Long getCcmId() {
        return ccmId;
    }

    public void setCcmId(Long ccmId) {
        this.ccmId = ccmId;
    }

    public Set<Long> getIcmSetPreCopy() {
        return icmSetPreCopy;
    }

    public void setIcmSetPreCopy(Set<Long> icmSetPreCopy) {
        this.icmSetPreCopy = icmSetPreCopy;
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    public void setStarter(RelationNode starter) {
        this.starter = starter;
    }

    public void setEnder(ClassNode ender) {
        this.ender = ender;
    }
}
