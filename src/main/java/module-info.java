module org.openmarkov.core {
	requires org.openmarkov.plugin;
	requires transitive java.desktop;
	requires commons.math3;
	requires jeval;
	requires colt;
	requires org.apache.logging.log4j;
		
	exports org.openmarkov.core.action;
	exports org.openmarkov.core.dt;
	exports org.openmarkov.core.exception;
	exports org.openmarkov.core.inference;
	exports org.openmarkov.core.inference.heuristic;
	exports org.openmarkov.core.inference.tasks;
	exports org.openmarkov.core.io.database.exception;
	exports org.openmarkov.core.model.graph;
	exports org.openmarkov.core.model.network;
	exports org.openmarkov.core.model.network.constraint;
	exports org.openmarkov.core.model.network.constraint.annotation;
	exports org.openmarkov.core.model.network.factory;
	exports org.openmarkov.core.model.network.modelUncertainty;
	exports org.openmarkov.core.model.network.potential;
	exports org.openmarkov.core.model.network.potential.canonical;
	exports org.openmarkov.core.model.network.potential.operation;
	exports org.openmarkov.core.model.network.potential.plugin;
	exports org.openmarkov.core.model.network.potential.treeadd;
	exports org.openmarkov.core.model.network.type;
	exports org.openmarkov.core.model.network.type.plugin;
	exports org.openmarkov.core.inference.annotation;
	exports org.openmarkov.core.io;
	exports org.openmarkov.core.io.database;
	exports org.openmarkov.core.io.database.plugin;
	exports org.openmarkov.core.io.format.annotation;	
	exports org.openmarkov.core.oopn;
	exports org.openmarkov.core.oopn.action;
	exports org.openmarkov.core.oopn.exception;
	
	

}
