CURRENT_DATE=`date '+%Y/%m/%d'`
mvn clean install
java -jar ./target/spring-batch-testing-0.0.1-SNAPSHOT.jar "item=shoes" "run.date(date)=$CURRENT_DATE" "count=4"