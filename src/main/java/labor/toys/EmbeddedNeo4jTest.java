package labor.toys;

import labor.nodeRel.DegreeTypeRel;
import labor.nodeRel.DimensionTypeRel;
import org.neo4j.dbms.api.DatabaseManagementService;
import org.neo4j.dbms.api.DatabaseManagementServiceBuilder;
import org.neo4j.graphdb.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.neo4j.configuration.GraphDatabaseSettings.DEFAULT_DATABASE_NAME;

public class EmbeddedNeo4jTest {
    GraphDatabaseService graphDb;
    Relationship relationship;

    /**
     * Starting a database server is an expensive operation,
     * so do not start up a new instance every time you need to interact with the database.
     * The instance can be shared by multiple threads,
     * and transactions are thread confined.
     */
    public void startNeo4j(String dbPath, DegreeTypeRel degreeType, DimensionTypeRel dimensionType){

        Path path = Paths.get(dbPath);
        DatabaseManagementService managementService = new DatabaseManagementServiceBuilder(path).build();
        graphDb = managementService.database( DEFAULT_DATABASE_NAME );
        registerShutdownHook(managementService);

        //addNodes(graphDb);
        //myQuery(graphDb);
        csvImport(graphDb, degreeType, dimensionType);

        // Finally, shut down the database server when the application finishes:
        managementService.shutdown();
    }

    private void csvImport(GraphDatabaseService graphDb, DegreeTypeRel degreeType, DimensionTypeRel dimensionType){
        // time_all[1] is valid time, time_all[0] is transaction time
        int dimension_num = dimensionType.equals(DimensionTypeRel.VALID_TIME)? 1 : 0;
        // if tx, '(' needs to be deleted, if valid, ')' needs to be deleted
        String substring_dim = dimensionType.equals(DimensionTypeRel.VALID_TIME)?
                "WITH line, time_interval[0] AS t_from, replace(time_interval[1], ')', '') AS t_to\n":
                "WITH line, replace(time_interval[0], '(', '') AS t_from, time_interval[1] AS t_to\n";

        String linesFromCSV = "LOAD CSV FROM 'file:///Users/lxx/Desktop/Data_temporal/citibike_edges_1.csv' AS line FIELDTERMINATOR ';'\n" +
                              //"WITH line LIMIT 10\n" +
                              "WITH line, split(line[6], \"),(\") AS time_all\n" +
                              "WITH line, split(time_all[" + dimension_num + "], ',') AS time_interval\n" +
                              substring_dim;

        switch (degreeType){
            case IN:
                String importFromFile_IN = linesFromCSV +
                                           "CREATE (:vertex {VertexId: line[3], from: t_from, to: t_to})\n";
                myTry(graphDb, importFromFile_IN);
                break;

            case OUT:
                String importFromFile_OUT = linesFromCSV +
                                            "CREATE (:vertex {VertexId: line[2], from: t_from, to: t_to})\n";
                myTry(graphDb, importFromFile_OUT);
                break;

            case BOTH:
                String importFromFile_BOTH = linesFromCSV +
                                             "CREATE (:vertex {VertexId: line[3], from: t_from, to: t_to})\n" +
                                             "CREATE (:vertex {VertexId: line[2], from: t_from, to: t_to})\n";
                myTry(graphDb, importFromFile_BOTH);
                break;

            default:
                throw new IllegalArgumentException("Invalid vertex degree type [" + degreeType + "].");
        }
    }

    private void myTry(GraphDatabaseService graphDb, String str){
        try (Transaction tx = graphDb.beginTx()){
            System.out.println(tx.execute(str).resultAsString());
            //System.out.println(tx.execute("MATCH (n) RETURN n.VertexId, n.from, n.to").resultAsString());
            tx.commit();
        }
    }

    private void myQuery(GraphDatabaseService graphDb){
        try ( Transaction tx = graphDb.beginTx() ) {
            // Queries
            System.out.println("Query start");
            String q = "MATCH(n) RETURN count(*)";
            Result result = tx.execute(q);
            String n = result.resultAsString();
            System.out.println(n);
        }
    }

    private void addNodes(GraphDatabaseService graphDb){
        try ( Transaction tx = graphDb.beginTx() ) {
            // Database operations go here
            Node firstNode = tx.createNode();
            firstNode.setProperty( "message", "Hello, " );
            Node secondNode = tx.createNode();
            secondNode.setProperty( "message", "World!" );

            relationship = firstNode.createRelationshipTo( secondNode, RelTypes.KNOWS );
            relationship.setProperty( "message", "brave Neo4j " );

            System.out.print( firstNode.getProperty( "message" ) );
            System.out.print( relationship.getProperty( "message" ) );
            System.out.print( secondNode.getProperty( "message" ) );

            tx.commit();
        }
    }

/* Remove nodes
    private void removeNodes(GraphDatabaseService graphDb){
        try( Transaction tx = graphDb.beginTx()){
            Node firstNode = tx.getNodeById( firstNode.getId() );
            Node secondNode = tx.getNodeById( secondNode.getId() );
            firstNode.getSingleRelationship( RelTypes.KNOWS, Direction.OUTGOING ).delete();
            firstNode.delete();
            secondNode.delete();
            tx.commit();
        }
    }
*/
    /**
     * As seen, you can register a shutdown hook that will make sure the database shuts down when the JVM exits.
     * @param managementService The Service to register
     */
    private static void registerShutdownHook( final DatabaseManagementService managementService )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running application).
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
                managementService.shutdown();
            }
        } );
    }
}
