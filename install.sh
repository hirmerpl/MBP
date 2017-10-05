!/bin/sh
sudo sh -c "echo '127.0.0.1' $(hostname) >> /etc/hosts";
sudo apt-get -qy update;
#sudo apt-get -qy upgrade;

#Installing Java8, pip, python mqtt client
sudo apt-get install -qy openjdk-8-jdk;
sudo apt-get install -qy python-pip;
sudo pip install paho-mqtt;
sudo pip install pymongo;

# Install Mosquitto Broker
sudo apt-get install -y mosquitto;

# Install and start MongoDB 
sudo apt-get -y install mongodb-server;
sudo /etc/init.d/mongodb start;
sudo /etc/init.d/mongodb status;

# Install Tomcat 9
sudo apt-get install -y tomcat8;
sudo apt-get install -y git;
sudo apt-get install -y maven;

# checkout repository
sudo git clone https://github.com/ana-silva/connde.git
cd connde/java-projects/restful-connde
sudo mvn clean install

# deploy war to Tomcat
sudo mv target/restful-connde-1.0-SNAPSHOT.war /var/lib/tomcat8/webapps/MBP.war
echo "Installation finished"