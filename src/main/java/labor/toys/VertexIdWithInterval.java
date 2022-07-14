package labor.toys;

import labor.nodeRel.DegreeTypeRel;
import labor.nodeRel.DimensionTypeRel;
import org.neo4j.graphdb.Node;

import java.util.Objects;

public class VertexIdWithInterval {
    private final DegreeTypeRel degreeType;
    private final DimensionTypeRel dimensionTypeRel;
    /**
     * @param degreeType IN, OUT or BOTH
     * @param dimensionTypeRel VALID_TIME or TRANSACTION_TIME
     */
    public VertexIdWithInterval(DegreeTypeRel degreeType, DimensionTypeRel dimensionTypeRel){
        this.degreeType = Objects.requireNonNull(degreeType);
        this.dimensionTypeRel = Objects.requireNonNull(dimensionTypeRel);
    }

    // the param should be the graphDB
    public void subNode(Node edgeNode){
    }
}
