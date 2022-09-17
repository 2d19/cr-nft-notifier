# cr-nft-notifier

### Running locally in IDE
- provide the following environment properties: TG_CHAT_ID, TG_URL, OPENSEA_COLLECTION_URL, COLLECTIONS, MINIMUM_FLOOR_PRICE.
- run `App` class.
- the app should start sending requests to opensea and check the collections of your interest, which will be sent to the provided telegram chat.

### Testing
- Docker should be running for the integration tests.

### Local manual testing requirements
- docker
- localstack: https://hub.docker.com/r/localstack/localstack
- sam: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/what-is-sam.html
- samlocal: https://github.com/localstack/aws-sam-cli-local

### Local testing with provided template.yml and localstack:

- samlocal init
- samlocal build
- localstack start
- samlocal deploy --guided
- sam local invoke

### Run on aws
- upload jar to aws, via sam cli or aws portal.
- configure a CloudWatch event (EventBridge) to execute the lambda via cron.
