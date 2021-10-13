CURRENT_DATE=`date '+%Y/%m/%d'`
LESSON=$(basename $PWD)
mvn clean install -DskipTests
java -jar ./target/spring-batch-testing-0.0.1-SNAPSHOT.jar "run.date(date)=$CURRENT_DATE" "lesson=$LESSON" type=$1