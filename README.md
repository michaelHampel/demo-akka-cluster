# Akka Cluster Demo #

added different configurations to run on Kubernetes - locally tested with Minikube
For Kubernetes you can use
- [DNS-Discovery](https://developer.lightbend.com/docs/akka-management/current/discovery.html#discovery-method-akka-dns-discovery)  or
- the [Kubernetes API](https://developer.lightbend.com/docs/akka-management/current/discovery.html#discovery-method-kubernetes-api)

from the [Akka Management](https://developer.lightbend.com/docs/akka-management/current/index.html) project to start your Akka Cluster.

## Endpoints

- GET /self-address: cluster address of the contacted node
- GET /cluster/members: all cluster members

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
