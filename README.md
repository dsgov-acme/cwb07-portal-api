# dsgov-acme Portal API

The Portal API provides back-end support to the DSGov web portals. This includes:

* Providing session logout functionality
* Provides support to process a batch of client side logs
* (Coming Soon) Handling of feature toggle live state.

## Prerequisites

Make sure you have the following installed:

1. Java 11+
2. Docker
3. Setup and configure minikube (using [This setup](https://github.com/dsgov-acme/cwb07-local-environment))

## Run Locally

1. To just spin up the service in `minikube`, run this command: `skaffold run`
2. [view docs](http://api.cwb07.test/portal/swagger-ui/index.html)

## Develop Locally

1. Setup and configure minikube (using [This setup](https://github.com/dsgov-acme/cwb07-local-environment))
2. In a standalone terminal, run: `skaffold dev`
3. You should eventually have console output similar to this:
![Skaffold Dev Log](docs/assets/skaffold-dev-log.png)
4. As you make code changes, Skaffold will rebuild the container image and deploy it to your local `minikube` cluster.
5. Once the new deployment is live, you can re-generate your Postman collection to test your new API changes!

To exit `skaffold dev`, in the terminal where you executed the command, hit `Ctrl + C`.

**NOTE: This will terminate your existing app deployment in minikube.**

### Documentation

* [tools and frameworks](./docs/tools.md)

## Configuration Parameters

Here are the key configuration parameters for the application:
### Helm

#### Network
- host: `<api-domain-name>`
- applicationPort: `<k8s-application-container-port>`
- servicePort: `<k8s-service-port>`
- contextPath: `<k8s-ingress-context-path>`
- readinessProbe.path: `<k8s-readiness-probe-path>`

#### Environment Variables
- ALLOWED_ORIGINS: `<allowed-origins>`
- CERBOS_URI: `<cerbos-uri>`
- GCP_PROJECT_ID: `<gcp-project-id>`
- OTEL_SAMPLER_PROBABILITY: `<opentelemetry-sampler-probability>`

### Gradle

#### settings.gradle
- rootProject.name = `<project-name>`

#### gradle-wrapper.properties
- distributionBase=`<distribution-base>`
- distributionPath=`<distribution-path>`
- distributionUrl=`<distribution-url>`
- zipStoreBase=`<zip-store-base>`
- zipStorePath=`<zip-store-path>`
