# org.openmarkov.core

Módulo base de OpenMarkov. Contiene el modelo de dominio completo: redes probabilísticas, variables, potenciales, restricciones, sistema de ediciones con undo/redo, e inferencia base.

## Paquetes principales

```
org.openmarkov.core
├── model/
│   ├── network/             ProbNet, Node, Variable, Link, Finding, EvidenceCase, NetworkType
│   ├── graph/               Graph<T> genérico (grafo dirigido/no dirigido)
│   ├── network/potential/   30+ implementaciones de Potential (CPTs, utilidades)
│   │   ├── canonical/       ICIPotential y clases de soporte (ICIFamily, ICIModelType)
│   │   └── operation/       PotentialOperations, DiscretePotentialOperations, concurrent/
│   ├── network/constraint/  35+ restricciones de red (NoCycle, OnlyChanceNodes, etc.)
│   ├── network/type/        Tipos de red: BN, ID, LIMID, DBN, MDP, POMDP, DAN, etc.
│   └── decisiontree/        Representación de árboles de decisión
├── action/
│   ├── base/                PNEdit (base), PNESupport, EditsHistory, CompoundPNEdit, ConstraintChecker
│   │   └── linkEdits/       AddLinkEdit, RemoveLinkEdit, InvertLinkEdit, OrientLinkEdit…
│   ├── core/                37 ediciones concretas (AddNodeEdit, RemoveNodeEdit, etc.)
│   ├── command/             (reservado para futura extensión)
│   └── execution/           (reservado para futura extensión)
├── inference/
│   ├── InferenceAlgorithm   Base abstracta para todos los algoritmos
│   ├── InferenceOptions     Configuración de una ejecución de inferencia
│   └── tasks/               12 tareas concretas: Propagation, Evaluation, CEAnalysis, OptimalPolicies…
├── io/                      ProbNetReader / ProbNetWriter (interfaces abstractas + @ProbNetFormatType)
├── localize/                ClassLocalizable, StringDatabase, Languages (i18n)
├── expression/              Evaluación de expresiones sobre variables (VariableExpression)
├── exception/               30+ excepciones de dominio (jerarquía estructurada)
├── developmentStaticAnalysis/ @ImplementationRequirements, @BindLocalizations, @Immutable, @ToCheck
└── java/                    Utilidades: ArrayUtils, CloneUtils, EnumUtils, Lazy<T>, etc.
```

## Clases de dominio clave

### ProbNet
Red probabilística. Extiende `Graph<Node>`. Contiene nodos, arcos, potenciales, restricciones y metadatos.

```java
ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
net.addVariable(var);
net.addNode(node);
net.addLink(parent, child, true);          // true = directed
List<Node> nodes = net.getNodes(NodeType.CHANCE);
net.copy()                                 // copia profunda para inferencia
```

### Variable
Variable aleatoria, de decisión o de utilidad. Puede ser temporal.

```java
Variable v = new Variable("Rain", new State[]{ new State("yes"), new State("no") });
v.getVariableType()   // FINITE_STATES | NUMERIC | DISCRETIZED
v.isTemporal()
v.getTimeSlice()      // índice temporal en nombre, p.ej. "X[1]"
```

`additionalProperties: LinkedHashMap<String,String>` en `Variable`, `Node`, `State` y `Potential` para metadatos extensibles.

### Node
Nodo del grafo: contiene la Variable, sus Potentials, posición visual y propiedades.

```java
node.getVariable()
node.getPotentials()
node.getParents()    // List<Node>
node.getChildren()
node.getNodeType()   // CHANCE | DECISION | UTILITY
```

### Potential (jerarquía)
Base abstracta de CPTs y funciones de utilidad. Marcada con `@ImplementationRequirements`.

| Subclase | Uso |
|---|---|
| `TablePotential` | CPT discreta (más común); almacena `double[] values` con offsets |
| `UniformPotential` | Distribución uniforme |
| `DeltaPotential` | Determinista (spike en un valor) |
| `ConditionalGaussianPotential` | Variables continuas |
| `FunctionPotential` | Definida por expresión |
| `ICIPotential` | Formas canónicas (noisy-OR, noisy-MAX, etc.) — en subpaquete `canonical/` |
| `LinearCombinationPotential` | Agregación de utilidades |
| `ProductPotential` / `SumPotential` | Producto y suma de potenciales |
| `MaxPotential` / `MinPotential` / `MinMaxPotential` | Operaciones max/min |
| `StrategyTree` / `TreeADDPotential` / `SDAGStrategyTree` | Estrategias de decisión en árbol/ADD |
| `AugmentedProbTable` / `AugmentedProbTablePotential` | Incertidumbre de modelo |
| `ExponentialHazardPotential` / `WeibullHazardPotential` | Análisis de supervivencia |
| `ExponentialPotential` / `BinomialPotential` | Distribuciones paramétricas |
| `GLMPotential` / `GTablePotential` | Modelos lineales generalizados |
| `UnivariateDistrPotential` / `DiscretizedCauchyPotential` / `ExactDistrPotential` | Distribuciones univariadas |
| `SameAsPrevious` | Reutilización en redes temporales |
| `EvidencePotentials` | Absorción de evidencia |
| `CycleLengthShift` | Desplazamiento en redes cíclicas |
| `TuningPotential` | Ajuste/afinación de redes |

`PotentialRole`: `CONDITIONAL_PROBABILITY`, `UTILITY`, `LINK_RESTRICTION`

Las operaciones sobre `Potential` devuelven nuevas instancias (inmutabilidad). Existen versiones concurrentes en `potential/operation/concurrent/`.

### EvidenceCase / Finding
Conjunto de observaciones para inferencia.

```java
EvidenceCase ev = new EvidenceCase();
ev.addFinding(new Finding(variable, stateIndex));
```

## Sistema de ediciones (Undo/Redo)

Toda modificación de una `ProbNet` se hace mediante subclases de `PNEdit`:

```java
// Ejecutar una edición
AddNodeEdit edit = new AddNodeEdit(probNet, variable, nodeType, position);
probNet.doEdit(edit);   // valida restricciones, ejecuta, notifica listeners

// Transacción compuesta
CompoundPNEdit compound = new CompoundPNEdit(probNet);
compound.addEdit(edit1);
compound.addEdit(edit2);
probNet.doEdit(compound);
```

Ciclo completo: `PNEdit.executeEdit()` → `checkConstraintsWillBeMet()` → `doEdit()` → notifica `PNEditListener`.

`PNESupport` gestiona el historial de undo/redo y los listeners. Los `PNEdit` siempre se ejecutan vía `probNet.doEdit(edit)`, nunca directamente.

## Sistema de restricciones

```java
// Verificación fluida
new ConstraintChecker(net)
    .checkConstraint(NoCycleConstraint.class)
    .checkConstraint(NoSelfLoopConstraint.class)
    .buildAndThrow();   // lanza ConstraintViolatedException si hay violaciones
```

Restricciones más usadas:
```
NoCycle, NoSelfLoop, NoMultipleLinks, DistinctLinks,
OnlyChanceNodes, OnlyDirectedLinks, OnlyUndirectedLinks,
MaxNumParents, NoUtilityParent, NoMixedParents,
NoBackwardLink, NoRevelationArc, DistinctVariableNames,
ModelNetworkConstraint
```

## Tipos de red

```java
BayesianNetworkType.getUniqueInstance()
InfluenceDiagramType.getUniqueInstance()
LIMIDType.getUniqueInstance()
DynamicBayesianNetworkType.getUniqueInstance()
MarkovNetworkType.getUniqueInstance()
MDPType.getUniqueInstance()
POMDPType.getUniqueInstance()
DECPOMDPType.getUniqueInstance()
DecisionAnalysisNetworkType.getUniqueInstance()
MIDType.getUniqueInstance()        // Multi-agent ID
TuningNetworkType.getUniqueInstance()
```

Cada tipo define qué restricciones son obligatorias. Los tipos se registran como plugins con `@NetworkType`.

## Inferencia

```java
InferenceAlgorithm algo = InferenceManager.getDefaultAlgorithm(probNet);
algo.setPostResolutionEvidence(evidenceCase);
// La red se copia internamente; el original no se modifica
Propagation result = (Propagation) algo.query(new PropagationTask(), evidenceCase);
```

`InferenceOptions` controla: multicriterio (`MulticriteriaOptions`), análisis temporal (`TemporalOptions`), eliminación de variables, tasa de descuento, etc.

Tareas concretas en `inference/tasks/` (12):
```
Propagation, Evaluation, OptimalPolicies, ExpectedUtilityDecision,
CEAnalysis, CE_PSA, GenerateDecisionTree, TemporalEvolution,
SensAnMap, SensAnTornadoSpider, SensAnPlot, OptimalIntervention
```
`Task` y `TaskUtilities` son clases de soporte/base, no tareas invocables.

## Jerarquía de excepciones

El paquete `exception/` tiene ~30 clases estructuradas en dos jerarquías:

- **`OpenMarkovException`** — base checked para excepciones recuperables de dominio
  - `UserInputException` — agrupa errores causados por entrada inválida del usuario
  - `ParserException` — fallos SAX/IO durante parseo de ficheros
  - `ConstraintViolatedException`, `InvalidArgumentException`, `IncompatibleEvidenceException`, etc.
- **`OpenMarkovRuntimeException`** — base unchecked para errores de programación
  - `UnreachableException` — código que no debería alcanzarse (reemplaza `ThereIsNo{Next,Previous}EvidenceCaseException`)
  - `UnrecoverableException` — fallos no recuperables

Las excepciones implementan `IBundledOpenMarkovException` para localización.
Las clases que añaden mensajes localizados implementan también `IOpenMarkovException`.

## Plugin / extensibilidad

Los puntos de extensión están marcados con `@ImplementationRequirements`:

- **Potentials**: anotación `@PotentialType`; descubiertos por classgraph en `PotentialTypeManager`
- **InferenceAlgorithm**: subclases registradas via SPI (auto-service)
- **NetworkType**: subclases descubiertas por `NetworkTypeUtils`
- **PNConstraint**: 35+ implementaciones plugables, gestionadas por `ConstraintManager`
- **ProbNetReader/Writer**: anotación `@ProbNetFormatType`; formatos de archivo (XML, XDSL, net, etc.)

## Localización

```java
StringDatabase.getUniqueInstance().getString("key")
// Las clases implementan ClassLocalizable; el procesador de anotaciones
// (annotationProcessing) enlaza clases con sus strings en tiempo de compilación
// Ficheros XML: core/localize/core_en.xml, CoreExceptions_en.xml, etc.
```

## Dependencias clave

| Librería | Versión | Uso |
|---|---|---|
| commons-math3 | 3.6.1 | Numérica, estadística |
| colt | 1.2.0 | Álgebra lineal, arrays |
| jgrapht-core/ext | 1.5.2 | Algoritmos de grafos |
| jdom2 | 2.0.6.1 | Parseo/serialización XML |
| antlr4-runtime | 4.13.1 | Runtime parser |
| classgraph | 4.8.179 | Descubrimiento de plugins |
| gson | 2.13.2 | Serialización JSON |
| log4j-api/core | 2.25.3 | Logging |
| auto-service-annotations | 1.1.1 | Registro SPI |
| AssertJ | 4.0.0-M1 | Aserciones en tests |

## Tests

84 clases de test. Patrones habituales:

```java
@Test
void test() {
    ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
    // setup manual de nodos, variables, potenciales
    // ejecutar edición o inferencia
    // assert con tolerancia numérica (AssertJ)
}
```

Tests de inferencia en `InferenceAlgorithmBNTest`, `InferenceAlgorithmIDTest`, `InferenceAlgorithmDANTest`, etc.
Fixtures de red en `src/test/resources/nets/` (ficheros .xml para BN, ID, DAN, MDP, DBN, etc.).
