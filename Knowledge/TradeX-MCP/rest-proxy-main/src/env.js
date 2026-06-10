const path = require('path');
function configure(conf) {
  // conf.kafkaUrls = ['dev1tradex.local:9092'];
  conf.kafkaUrls = ['172.31.43.101:9092'];
  conf.zkUrl = ['172.31.43.101: 2181'];
  // conf.kafkaUrls = ['localhost:9092'];
  // conf.zkUrl = ['localhost: 2181'];
  conf.topic.configuration = 'configuration-1';
  conf.jwt.publicKeyFile = path.join(__dirname, "../../../tradex-key/dev/aaa/tradex/jwt-public.key") ;
  conf.jwt.domains = {
    tradex: {
      publicKeyFile: path.join(__dirname, "../../../tradex-key/dev/aaa/tradex/jwt-public.key")
    }
  };
  console.log(conf);
  return conf;
}

module.exports = configure;
