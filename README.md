# Akka Cluster Demo #

added different configurations to run on Kubernetes
locally tested with minikube

For Kubernetes you can use
- [DNS-Discovery](https://developer.lightbend.com/docs/akka-management/current/discovery.html#discovery-method-akka-dns-discovery)  or
- the [Kubernetes API](https://developer.lightbend.com/docs/akka-management/current/discovery.html#discovery-method-kubernetes-api)

from the [Akka Management](https://developer.lightbend.com/docs/akka-management/current/index.html) project to start your Akka Cluster.

All yml files are in the kubernetes directory.

## Basic Setup

- Install [minikube](https://github.com/kubernetes/minikube)
- clone this project
- start minikube e.g.: `minikube start --memory=6000` if using hyperkit/xhyve [see](https://github.com/kubernetes/minikube/issues/951)
- set docker environment to minikube: `eval $(minikube docker-env)`
- change application.conf for either using dns or kubernetes discovery and to enable Artery for Akka remote.
- build the docker container and publish(locally): `sbt docker:publishLocal`
- when finished: `minikube stop`, `minikube delete` to cleanup...

## DNS discovery

- `kubectl create -f dns-deployment.yml`
This creates a deployment with 4 replicas of the dac demo app. Each pod exposes a port for
- Akka remoting
- Akka HTTP Management and
- a HTTP port for the app

Additionally a headless service for DNS discovery is deployed.

To verify either open the minikube dashboard and look at the logs of the deployed pods or use the commandline:
- `POD=$(kubectl get pods | grep dac | grep Running | head -n1 | awk '{ print $1 }'); echo $POD`
- `kubectl logs $POD -f`

If the returned pod id is the oldest node you should see smthg. like:

```
16:30:33 INFO  Cluster(akka://dac) [akka.cluster.Cluster(akka://dac)] -
Cluster Node [akka.tcp://dac@172.17.0.10:2552] -
Leader is moving node [akka.tcp://dac@172.17.0.8:2552] to [Up]
```

for each replica's port.

## Kubernetes API Discovery with Akka Netty remote

- `kubectl create -f tcp-deployment.yml`
- there is no headless service necessary - the Kubernetes API is used to find the nodes for the Akka cluster
- the rest works as described for DNS discovery

## Kubernetes API Discovery with artery

- `kubectl create -f artery-deployment.yml`
- Aeron uses shared memory - default size is 64m which is not enough. For docker the shm size is easily configurable(look at      dockerfile), but there is no config value for a Kubernetes pod. [see](https://github.com/kubernetes/kubernetes/issues/28272), but there is a workaround [here](https://docs.openshift.org/latest/dev_guide/shared_memory.html)
- also when using Artery we sometimes see very high memory/CPU load and Cluster formation fails - further investigations.

## Services

Configurations to access the Akka HTTP Mgmt API as well as the HTTP API of the DAC service:

## NodePort Services

- `kubectl create -f nodePort-services.yml`
- to verify: `kubectl get services`
- to access, first get the URL of the exposed services: `minikube service --url cluster-http-mgmt`
- curl the returned URL with /cluster/members

Example:

```
{"selfNode":"akka.tcp://dac@172.17.0.10:2552",
"leader":"akka.tcp://dac@172.17.0.10:2552",
"oldest":"akka.tcp://dac@172.17.0.10:2552","unreachable":[],
"members":[
{"node":"akka.tcp://dac@172.17.0.10:2552","nodeUid":"2028087476","status":"Up","roles":["dc-default"]},
{"node":"akka.tcp://dac@172.17.0.11:2552","nodeUid":"-1150166493","status":"Up","roles":["dc-default"]},
{"node":"akka.tcp://dac@172.17.0.8:2552","nodeUid":"1696269376","status":"Up","roles":["dc-default"]},
{"node":"akka.tcp://dac@172.17.0.9:2552","nodeUid":"782644251","status":"Up","roles":["dc-default"]}]}
```

## Ingress

- enable the Ingress(Nginx) addon for Minikube: `minikube addons enable ingress`, verify: `minikube addons list`
- deploy routing config for ingress: `kubectl create -f ingress.yml`
- deploy our services: `kubectl create -f ingress-services.yml` (no NodePort config necessary - access through ingress..)
- update your hosts file for domain routing:

`echo "$(minikube ip) mh.com" | sudo tee -a /etc/hosts`

- curl an endpoint, e.g.: `curl http://mh.com/hello`, should return `Hello, from DAC Service..`

## Docker Compose with DNS discovery(and Artery)

- `docker-compose up -d` - This will start a single Akka cluster node
- `docker-compose scale dac=4`- scale to 4 nodes, all joining the same cluster
- `docker-compose logs` to see if everything worked
- `docker stop _dac_1` stop a node - in another terminal window
- `docker start _dac_1` back to 4 nodes again..
- `docker-compose stop`, `docker-compose rm` to cleanup


## Endpoints

- GET /self-address: cluster address of the contacted node
- GET /cluster/members: all cluster members
- GET /hello: returns `Hello from DAC service..``

Example:

```
~/projekte/github/rocks.heikoseeberger/demo-akka-cluster(master ✔) http :8000/self-address
HTTP/1.1 200 OK
Content-Length: 26
Content-Type: text/plain; charset=UTF-8
Date: Thu, 18 Jan 2018 12:34:49 GMT
Server: akka-http/10.0.11

akka://dac@127.0.0.1:10000
```

## Run standalone

Either run from within sbt with `reStart` (from sbt-revolver) or even better with the provided
command aliases `r0` or `r1`. Or create a universal binary with `universal:packageBin`
(from sbt-native-packager) and execute the launcher at `bin/demo-akka-cluster`.

Provide the following JVM arguments:

- Node zero (like in `r0`):
  - -Ddac.api.port=8000
  - -Dakka.management.http.port=20000
  - -Dakka.remote.artery.canonical.hostname=127.0.0.1
  - -Dakka.remote.artery.canonical.port=10000
  - -Dakka.cluster.seed-nodes.0=akka://dac@127.0.0.1:10000
- Node one (likle in `r1`):
  - -Ddac.api.port=8001
  - -Dakka.management.http.port=20001
  - -Dakka.remote.artery.canonical.hostname=127.0.0.1
  - -Dakka.remote.artery.canonical.port=10001
  - -Dakka.cluster.seed-nodes.0=akka://dac@127.0.0.1:10000

## Run in Docker

Just use the provided Docker Compose file.

## Contribution policy ##

Contributions via GitHub pull requests are gladly accepted from their original author. Along with
any pull requests, please state that the contribution is your original work and that you license
the work to the project under the project's open source license. Whether or not you state this
explicitly, by submitting any copyrighted material via pull request, email, or other means you
agree to license the material under the project's open source license and warrant that you have the
legal authority to do so.

## License ##

This code is open source software licensed under the
[Apache-2.0](http://www.apache.org/licenses/LICENSE-2.0) license.
