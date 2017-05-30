import org.nd4j.linalg.factory.Nd4j

println(Nd4j.create(Array(0.1,0.2,0.3)).reshape(3,-1).shape().mkString(" "))