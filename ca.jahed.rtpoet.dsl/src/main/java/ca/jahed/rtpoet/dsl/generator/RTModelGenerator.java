package ca.jahed.rtpoet.dsl.generator;

import ca.jahed.rtpoet.dsl.RTSLibrary;
import ca.jahed.rtpoet.dsl.rt.*;
import ca.jahed.rtpoet.dsl.rt.Class;
import ca.jahed.rtpoet.dsl.rt.Enumeration;
import ca.jahed.rtpoet.dsl.rt.Package;
import ca.jahed.rtpoet.rtmodel.*;
import ca.jahed.rtpoet.rtmodel.builders.*;
import ca.jahed.rtpoet.rtmodel.builders.sm.*;
import ca.jahed.rtpoet.rtmodel.sm.*;
import ca.jahed.rtpoet.rtmodel.types.RTType;
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTBoolean;
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTFloat;
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTInteger;
import ca.jahed.rtpoet.rtmodel.types.primitivetype.RTString;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import java.util.*;

public class RTModelGenerator {
    private final Map<EObject, RTElement> cache = new HashMap<>();

    public RTModel doGenerate(Resource input) {
        RTSLibrary.load(input.getResourceSet());
        cache.putAll(RTSLibrary.getRtsClasses());
        cache.putAll(RTSLibrary.getRtsProtocols());
        cache.putAll(RTSLibrary.getRtsSignals());

        Model model = (Model) input.getContents().get(0);
        return (RTModel) generate(model);
    }

    private RTElement generate(EObject eObj) {
        RTElement result;
        if(eObj instanceof Package)
            result = cache.getOrDefault(eObj, generatePackage((Package) eObj));
        else if(eObj instanceof Capsule)
            result = cache.getOrDefault(eObj, generateCapsule((Capsule) eObj));
        else if(eObj instanceof Class)
            result = cache.getOrDefault(eObj, generateClass((Class) eObj));
        else if(eObj instanceof Enumeration)
            result = cache.getOrDefault(eObj, generateEnumeration((Enumeration) eObj));
        else if(eObj instanceof Artifact)
            result = cache.getOrDefault(eObj, generateArtifact((Artifact) eObj));
        else if(eObj instanceof Protocol)
            result = cache.getOrDefault(eObj, generateProtocol((Protocol) eObj));
        else if(eObj instanceof Attribute)
            result = cache.getOrDefault(eObj, generateAttribute((Attribute) eObj));
        else if(eObj instanceof Operation)
            result = cache.getOrDefault(eObj, generateOperation((Operation) eObj));
        else if(eObj instanceof Port)
            result = cache.getOrDefault(eObj, generatePort((Port) eObj));
        else if(eObj instanceof Part)
            result = cache.getOrDefault(eObj, generatePart((Part) eObj));
        else if(eObj instanceof Signal)
            result = cache.getOrDefault(eObj, generateSignal((Signal) eObj));
        else if(eObj instanceof Parameter)
            result = cache.getOrDefault(eObj, generateParameter((Parameter) eObj));
        else if(eObj instanceof Return)
            result = cache.getOrDefault(eObj, generateReturn((Return) eObj));
        else if(eObj instanceof Type)
            result = cache.getOrDefault(eObj, generateType((Type) eObj));
        else if(eObj instanceof Connector)
            result = cache.getOrDefault(eObj, generateConnector((Connector) eObj));
        else if(eObj instanceof StateMachine)
            result = cache.getOrDefault(eObj, generateStateMachine((StateMachine) eObj));
        else if(eObj instanceof State)
            result = cache.getOrDefault(eObj, generateState((State) eObj));
        else if(eObj instanceof Transition)
            result = cache.getOrDefault(eObj, generateTransition((Transition) eObj));
        else if(eObj instanceof Trigger)
            result = cache.getOrDefault(eObj, generateTrigger((Trigger) eObj));
        else
            result = cache.getOrDefault(eObj, generateModel((Model) eObj));

        cache.put(eObj, result);
        return result;
    }

    private RTModel generateModel(Model model) {
        Optional<Capsule> topCapsule = model.getCapsules().stream().filter(Capsule::isTop).findFirst();
        RTModelBuilder builder = topCapsule.map(capsule ->
                RTModel.builder(model.getName(), (RTCapsule) generate(capsule)))
                .orElseGet(() -> RTModel.builder(model.getName(), null));

        model.getPackages().forEach(pkg -> builder.pkg((RTPackage) generate(pkg)));
        model.getCapsules().forEach(capsule -> builder.capsule((RTCapsule) generate(capsule)));
        model.getClasses().forEach(clazz -> builder.klass((RTClass) generate(clazz)));
        model.getEnumerations().forEach(enumeration -> builder.enumeration((RTEnumeration) generate(enumeration)));
        model.getArtifacts().forEach(artifact -> builder.artifact((RTArtifact) generate(artifact)));
        model.getProtocols().forEach(protocol -> builder.protocol((RTProtocol) generate(protocol)));
        return builder.build();
    }

    private RTPackage generatePackage(Package pkg) {
        RTPackageBuilder builder = RTPackage.builder(pkg.getName());
        pkg.getPackages().forEach(p -> builder.pkg((RTPackage) generate(p)));
        pkg.getCapsules().forEach(capsule -> builder.capsule((RTCapsule) generate(capsule)));
        pkg.getClasses().forEach(clazz -> builder.klass((RTClass) generate(clazz)));
        pkg.getEnumerations().forEach(enumeration -> builder.enumeration((RTEnumeration) generate(enumeration)));
        pkg.getArtifacts().forEach(artifact -> builder.artifact((RTArtifact) generate(artifact)));
        pkg.getProtocols().forEach(protocol -> builder.protocol((RTProtocol) generate(protocol)));
        return builder.build();
    }

    private RTCapsule generateCapsule(Capsule capsule) {
        RTCapsuleBuilder builder = RTCapsule.builder(capsule.getName());
        capsule.getAttributes().forEach(attribute -> builder.attribute((RTAttribute) generate(attribute)));
        capsule.getOperations().forEach(operation -> builder.operation((RTOperation) generate(operation)));
        capsule.getParts().forEach(part -> builder.part((RTCapsulePart) generate(part)));
        capsule.getPorts().forEach(port -> builder.port((RTPort) generate(port)));
        capsule.getConnectors().forEach(connector -> builder.connector((RTConnector) generate(connector)));
        if(capsule.getStateMachine() != null) builder.statemachine((RTStateMachine) generate(capsule.getStateMachine()));
        return builder.build();
    }

    private RTClass generateClass(Class clazz) {
        RTClassBuilder builder = RTClass.builder(clazz.getName());
        clazz.getAttributes().forEach(attribute -> builder.attribute((RTAttribute) generate(attribute)));
        clazz.getOperations().forEach(operation -> builder.operation((RTOperation) generate(operation)));
        return builder.build();
    }

    private RTEnumeration generateEnumeration(Enumeration enumeration) {
        RTEnumerationBuilder builder = RTEnumeration.builder(enumeration.getName());
        enumeration.getLiterals().forEach(builder::literal);
        return builder.build();
    }

    private RTArtifact generateArtifact(Artifact artifact) {
        RTArtifactBuilder builder = RTArtifact.builder(artifact.getName());
        if(artifact.getFile() != null) builder.fileName(artifact.getFile());
        return builder.build();
    }

    private RTProtocol generateProtocol(Protocol protocol) {
        RTProtocolBuilder builder = RTProtocol.builder(protocol.getName());
        protocol.getSignals().forEach(signal -> {
            if(signal.getKind().equals("in")) builder.input((RTSignal) generate(signal));
            else if(signal.getKind().equals("out")) builder.output((RTSignal) generate(signal));
            else builder.inOut((RTSignal) generate(signal));
        });
        return builder.build();
    }

    private RTAttribute generateAttribute(Attribute attribute) {
        RTAttributeBuilder builder = RTAttribute.builder(attribute.getName(), (RTType) generate(attribute.getType()));
        if(attribute.getUpperBound() != null) builder.replication(attribute.getUpperBound().getValue());

        if(attribute.getVisibility() != null) {
            if(attribute.getVisibility().equals("public")) builder.publicVisibility();
            else if(attribute.getVisibility().equals("private")) builder.privateVisibility();
            else builder.protectedVisibility();
        }

        return builder.build();
    }

    private RTOperation generateOperation(Operation operation) {
        RTOperationBuilder builder = RTOperation.builder(operation.getName());
        operation.getParameters().forEach(parameter -> builder.parameter((RTParameter) generate(parameter)));
        if(operation.getReturn() != null) builder.ret((RTParameter) generate(operation.getReturn()));
        if(operation.getBody() != null) builder.action(RTAction.builder(extractActionCode(operation.getBody())).build());

        if(operation.getVisibility() != null) {
            if(operation.getVisibility().equals("public")) builder.publicVisibility();
            else if(operation.getVisibility().equals("private")) builder.privateVisibility();
            else builder.protectedVisibility();
        }

        return builder.build();
    }

    private RTPort generatePort(Port port) {
        RTPortBuilder builder = RTPort.builder(port.getName(), (RTProtocol) generate(port.getType()));
        if(port.isConjugate()) builder.conjugate();
        if(port.getRegistrationOverride() != null) builder.registrationOverride(port.getRegistrationOverride());
        if(port.getUpperBound() != null) builder.replication(port.getUpperBound().getValue());

        if(port.getKind() != null) {
            if(port.getKind().equals("internal")) builder.internal();
            else if(port.getKind().equals("sap")) builder.sap();
            else if(port.getKind().equals("spp")) builder.spp();
            else builder.external();
        }

        if(port.getRegistration() != null) {
            if(port.getRegistration().equals("app")) builder.appRegistration();
            else if(port.getKind().equals("autolocked")) builder.autoLockedRegistration();
            else builder.autoRegistration();
        }

        return builder.build();
    }

    private RTCapsulePart generatePart(Part part) {
        RTCapsulePartBuilder builder = RTCapsulePart.builder(part.getName(), (RTCapsule) generate(part.getType()));
        if(part.getUpperBound() != null) builder.replication(part.getUpperBound().getValue());

        if(part.getKind() != null) {
            if(part.getKind().equals("optional")) builder.optional();
            else if(part.getKind().equals("plugin")) builder.plugin();
            else builder.fixed();
        }

        return builder.build();
    }

    private RTSignal generateSignal(Signal signal) {
        RTSignalBuilder builder = RTSignal.builder(signal.getName());
        signal.getParameters().forEach(parameter -> builder.parameter((RTParameter) generate(parameter)));
        return builder.build();
    }

    private RTParameter generateParameter(Parameter parameter) {
        RTParameterBuilder builder = RTParameter.builder(parameter.getName(), (RTType) generate(parameter.getType()));
        if(parameter.getUpperBound() != null) builder.replication(parameter.getUpperBound().getValue());
        return builder.build();
    }

    private RTParameter generateReturn(Return parameter) {
        RTParameterBuilder builder = RTParameter.builder((RTType) generate(parameter.getType()));
        if(parameter.getUpperBound() != null) builder.replication(parameter.getUpperBound().getValue());
        return builder.build();
    }

    private RTType generateType(Type type) {
        if(type.getTypeRef() != null) {
            if(type.getTypeRef() instanceof Class)
                return (RTType) generate(type.getTypeRef());
            else
                return (RTType) generate(type.getTypeRef());
        }

        if(type instanceof PrimitiveType) {
            PrimitiveType pt = (PrimitiveType) type;
            if(pt.getName().equals("string"))
                return RTString.INSTANCE;
            if(pt.getName().equals("float"))
                return RTFloat.INSTANCE;
            if(pt.getName().equals("boolean"))
                return RTBoolean.INSTANCE;
        }

        return RTInteger.INSTANCE;
    }

    private RTConnector generateConnector(Connector connector) {
        return RTConnector.builder(
                generateConnectorEnd(connector.getPart1(), connector.getPort1()),
                generateConnectorEnd(connector.getPart2(), connector.getPort2())
        ).build();
    }

    private RTConnectorEnd generateConnectorEnd(Part part, Port port) {
        if(part != null)
            return RTConnectorEnd.builder((RTPort) generate(port), (RTCapsulePart) generate(part)).build();
        return RTConnectorEnd.builder((RTPort) generate(port), null).build();
    }

    private RTStateMachine generateStateMachine(StateMachine stateMachine) {
        RTStateMachineBuilder builder = RTStateMachine.builder();
        stateMachine.getSubstates().forEach(state -> builder.state((RTGenericState) generate(state)));
        stateMachine.getTransitions().forEach(transition -> builder.transition((RTTransition) generate(transition)));
        return builder.build();
    }

    private RTGenericState generateState(State state) {
        if(state instanceof PseudoState) return generatePseudoState((PseudoState) state);
        if(state instanceof CompositeState) return generateCompositeState((CompositeState) state);
        return generateSimpleState((SimpleState) state);
    }

    private RTGenericState generatePseudoState(PseudoState pseudoState) {
        if(pseudoState instanceof EntryPoint) return RTPseudoState.entryPoint(pseudoState.getName()).build();
        if(pseudoState instanceof ExitPoint) return RTPseudoState.exitPoint(pseudoState.getName()).build();
        if(pseudoState instanceof ChoicePoint) return RTPseudoState.choice(pseudoState.getName()).build();
        if(pseudoState instanceof JunctionPoint) return RTPseudoState.junction(pseudoState.getName()).build();
        if(pseudoState instanceof DeepHistory) return RTPseudoState.history(pseudoState.getName()).build();
        return RTPseudoState.initial(pseudoState.getName()).build();
    }

    private RTGenericState generateSimpleState(SimpleState state) {
        RTStateBuilder builder = RTState.builder(state.getName());
        if(state.getEntryAction() != null) builder.entry(extractActionCode(state.getEntryAction()));
        if(state.getExitAction() != null) builder.exit(extractActionCode(state.getExitAction()));
        return builder.build();
    }

    private RTGenericState generateCompositeState(CompositeState state) {
        RTCompositeStateBuilder builder = RTCompositeState.builder(state.getName());
        if(state.getEntryAction() != null) builder.entry(extractActionCode(state.getEntryAction()));
        if(state.getExitAction() != null) builder.exit(extractActionCode(state.getExitAction()));
        state.getSubstates().forEach(s -> builder.state((RTGenericState) generate(s)));
        state.getTransitions().forEach(transition -> builder.transition((RTTransition) generate(transition)));
        return builder.build();
    }

    private RTTransition generateTransition(Transition transition) {
        RTTransitionBuilder builder = RTTransition.builder((RTGenericState) generate(transition.getSource()),
                (RTGenericState) generate(transition.getTarget()));
        if(transition.getGuard() != null) builder.guard(extractActionCode(transition.getGuard().getBody()));
        if(transition.getActionChain() != null) builder.guard(extractActionCode(transition.getActionChain().getBody()));
        transition.getTriggers().forEach(trigger -> builder.trigger((RTTrigger) generate(trigger)));
        return builder.build();
    }

    private RTTrigger generateTrigger(Trigger trigger) {
        RTTriggerBuilder builder = RTTrigger.builder((RTSignal) generate(trigger.getSignal()),
                (RTPort) generate(trigger.getPorts().get(0)));
        for (int i = 1; i < trigger.getPorts().size(); i++)
            builder.port((RTPort) generate(trigger.getPorts().get(i)));
        return builder.build();
    }

    private String extractActionCode(String code) {
        if(code.length() <= 2)
            return "";
        return code.substring(1, code.length()-1);
    }
}
