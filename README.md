# pekko-stream-sse-example
typed actor を使ったSSE(Server Sent Events)

pekko Receptionist(pekko cluster) を使って、複数Play Framework サーバー間のメッセージをSSEする

## How to run
Start a Play app in the first terminal window:

```bash
sbt 'run -Dhttp.port=9000 -Dpekko.remote.artery.canonical.port=2550'
```

And open [http://localhost:9000/random1](http://localhost:9000/random1).

After that start another Play app in the second terminal window:

```bash
sbt 'run -Dhttp.port=9001 -Dpekko.remote.artery.canonical.port=2551'
```

And open [http://localhost:9001/random1](http://localhost:9001/random1).
