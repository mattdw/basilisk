# basilisk

A Clojure library for creating ring handlers that bundle together 
static resources. 

Basilisk is specifically intended for apps that are hosted behind some kind of proxy cache, such as Nginx or Varnish, that will trivially cache resources based on HTTP headers. It could also reasonably be used behind a caching Ring middleware.

The primary use case for Basilisk is combining multiple static files (typically located in a project's resources paths) that have some expensive combination and processing stage. For example this may be concatenating JavaScript files and then running it through a minification utility such as JSMin. Another example would be handling SCSS files.

By hosting the bundled resources behind a proxy cache, there does not need to be an explicit build step or a complex custom caching solution.

Finally Basilisk is intended to trivially work with external (non-JVM) tools such as sass or coffee script. The recommended approach here is to use `conch.sh` to wrap external commands as functions.


## Usage

Right now? Don't; you'll probably regret it.

## Thanks

The idea for this project came from Chas Emerick who suggested to resource and proxy cache driven model to me.

Thanks to the following for their feedback and and knowledge:

 * Anthony Grimes
 * Alan Malloy
 * Phil Hagelberg
 * Matt Wilson

## License

Copyright © 2012 Andrew Brehaut

Distributed under the Eclipse Public License, the same as Clojure.
