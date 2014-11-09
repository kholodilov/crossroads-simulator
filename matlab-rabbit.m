% create rabbitmq user for remote access:
% /usr/local/sbin/rabbitmqctl add_user test test
% /usr/local/sbin/rabbitmqctl set_user_tags test administrator
% /usr/local/sbin/rabbitmqctl set_permissions -p / test ".*" ".*" ".*"

% comment "NODE_IP_ADDRESS" in /usr/local/etc/rabbitmq/rabbitmq-env.conf

% run in MATLAB:

javaaddpath /home/dmitry/Development/Matlab/rabbitmq/rabbitmq-client.jar
cf = com.rabbitmq.client.ConnectionFactory;
cf.setHost('192.168.56.1'); 
cf.setUsername('test');
cf.setPassword('test');
c = cf.newConnection();
ch = c.createChannel();
msg = java.lang.String('{:x 0, :y 0, :t 99, :direction ns}').getBytes();
ch.basicPublish('', 'switch-events', [], msg);
