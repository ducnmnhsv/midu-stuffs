function config(conf) {
  conf.kafkaUrls = "172.31.145.221:9092";
  console.log(conf);
}

exports.default = config;