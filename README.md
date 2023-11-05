# Local Stack

## Enviroment 

## Variables

#### Powershell 
```
$env:AWS_ACCESS_KEY_ID="test"
$env:AWS_SECRET_ACCESS_KEY="test"
$env:AWS_DEFAULT_REGION="us-east-1"
$env:AWS_ENDPOINT_URL="http://localhost:4566"
```

#### Windows cmd
```
SET AWS_ACCESS_KEY_ID=test
SET AWS_SECRET_ACCESS_KEY=test
SET AWS_DEFAULT_REGION=us-east-1
SET AWS_ENDPOINT_URL=http://localhost:4566
```

## Docker

### Image
```agsl
docker pull localstack/localstack:2.3.2
```

### Container 
```agsl
docker run --rm -d -it -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack:2.3.2
```

### Compose 
```yaml
version: "3.8"

services:
  localstack:
    container_name: "${LOCALSTACK_DOCKER_NAME-localstack_main}"
    image: localstack/localstack
    ports:
      - "127.0.0.1:4566:4566"            # LocalStack Gateway
      - "127.0.0.1:4510-4559:4510-4559"  # external services port range
    environment:
      - DEBUG=${DEBUG-}
      - DOCKER_HOST=unix:///var/run/docker.sock
    volumes:
      - "${LOCALSTACK_VOLUME_DIR:-./volume}:/var/lib/localstack"
      - "/var/run/docker.sock:/var/run/docker.sock"
```

For more details on installation go to [Local Stack Intalation Guide](https://docs.localstack.cloud/getting-started/installation/)