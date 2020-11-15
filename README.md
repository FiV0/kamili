# kamili

**This is currently under construction.**

Kamili is supposed to become a Clojure(script) fullstack template. Currently it
is more of an exploration project.

## Usage

Install the npm modules
```
yarn install
```

Run clojure as
```
clj -A:dev -r
```

then simply run
```clj
(user/go)
```
If you want to connect to a cljs repl you can run the following
afterwards
```clj
(shadow/repl :app)
```

The app is then available at [http://localhost:8280](http://localhost:8280).

### Emacs
If you are using cider, you can simply do `clj-jack-in-clj&cljs` which automatically
runs the above commands.

## What's in the box?

### Integrant

[Integrant](https://github.com/weavejester/integrant) is a small framework
that lets you intialize your system from an [edn file](resources/kamili/system.edn).
The setup closely resembles the steps described by this
[lambdaisland](https://lambdaisland.com/blog/2019-12-11-advent-of-parens-11-integrant-in-practice)
post.

### Server side

There are currently two options to run the server.
The default is using [immutant](https://github.com/immutant/immutant),
[ring](https://github.com/ring-clojure/ring) and [reitit](https://github.com/metosin/reitit)
for routing.
The second is using [pedestal](https://github.com/pedestal/pedestal).

The main differenc between the two setups is that the first uses
middleware and second uses interceptors. I wanted to test both of
these and haven't decided on which one makes writing request/response
handling easier.
You can switch between these two options in the [edn file](resources/kamili/system.edn)
by toggling the `:reitit`/`:pedestal` option.

### Frontend

The frontend uses vanilla [re-frame](https://github.com/day8/re-frame) together
with [reitit-frontend](https://github.com/metosin/reitit).
[shadow-cljs](https://github.com/thheller/shadow-cljs) is used for hot-reloading
the cljs files.

### Logging

We use [glogi](https://github.com/lambdaisland/glogi) in the frontend and
[pedestal.log](https://github.com/pedestal/pedestal/tree/master/log) in the backend.
These are unified in a common [logging namespace](src/io/kamili/logging.cljc) so you don't
have to remember the details.

### Tests

### Dev

## License

Copyright © 2020 Finn Völkel

MIT License
