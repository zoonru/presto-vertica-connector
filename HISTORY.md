
    cd /opt/

    wget http://apache-mirror.rbc.ru/pub/apache/maven/maven-3/3.6.0/binaries/apache-maven-3.6.0-bin.zip

    unzip apache-maven-3.6.0-bin.zip

    export PATH=/opt/apache-maven-3.6.0/bin:$PATH

    apt update

    apt install -y default-jdk git

    wget https://www.vertica.com/client_drivers/9.1.x/9.1.1-0/vertica-client-9.1.1-0.x86_64.tar.gz

    tar xvf vertica-client-9.1.1-0.x86_64.tar.gz --strip-components=3 opt/vertica/java/vertica-jdbc-9.1.1-0.jar

    git clone --recursive https://github.com/zoonru/presto-vertica-connector.git

    cd /opt/presto-vertica-connector

    cp ../vertica-jdbc-9.1.1-0.jar /opt/presto-vertica-connector/src/main/resources/

    mvn clean install

    cd /opt/presto/

    cp -r plugin/mysql plugin/vertica
    rm plugin/vertica/mysql-connector*
    rm plugin/vertica/presto-mysql*

    cd /opt/presto-vertica-connector

    cp target/presto-vertica*.jar /opt/presto/plugin/vertica/
    cp ../vertica-jdbc-9.1.1-0.jar /opt/presto/plugin/vertica/
