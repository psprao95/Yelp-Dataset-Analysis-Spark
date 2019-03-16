import org.apache.spark.sql.functions._// Importing mutual-friends.txt
val input=sc.textFile("/FileStore/tables/mutual_friends-d6c0a.txt")

// Parsing. Format:Array[(String, Array[String])]
val temp = input.map(x => x.split("\t")).filter(x => x.length == 2).map(x => (x(0), x(1).split(",")))

// Mapping. Format:Array[List[(String, List[String])]]
val mapping=temp.map(x=>
{
  val userA=x._1
  val userAFriends=x._2.toList
  for (userB <- userAFriends) yield
  {
    if(userA.toInt < userB.toInt)
    {
      (userA+","+userB -> userAFriends)
    }
    else
    {
      (userB+","+userA->userAFriends)
    }
  }
  })

//Reducing
val reduce=mapping.flatMap(identity).map(x=>(x._1,x._2)).distinct.reduceByKey((x,y)=>x.intersect(y))

// Taking the top ten pairs
val output=reduce.map(x=>(x._1,x._2.length)).map(x=>(x._1.split(",")(0),x._1.split(",")(1),x._2))

// Making a dataframw
var pairs=output.toDF("userA_id","userB_id","mutual friend count")

display(pairs)
