(ns basilisk.bundle
  (:require [clojure.java.io :as io]
            [ring.util.response :as res]
            [clj-time.core :as time]
            clj-time.coerce
            clj-time.format))

(def rfc822 (:rfc822 clj-time.format/formatters))

(defn last-modified
  [^java.io.File file]
  (.lastModified file))

(defn file-date
  "returns the maximum last modified date for a set of file objects"
  [files]
  (->> (map last-modified files)
       (apply max)
       (clj-time.coerce/from-long)))

(def static-timestamp (constantly (time/now)))


(defn is-modified?
  "Given a joda (clj-time) time instance, and an HTTP-Time formated header
   returns a boolean if the latest timestamp is newer than the header value.
  "
  [latest modified-since-header]
  (try
    (let [since (clj-time.format/parse rfc822
                                       modified-since-header)]
      (time/after? since latest))
    (catch IllegalArgumentException e true)
    (catch NullPointerException e true)))


(defn bundle-response
  [files processor timestamp]
  (-> (res/response (processor files))
      (res/header "Last-Modified"
                  (clj-time.format/unparse rfc822
                                           timestamp))))


(defn bundle
  "bundle defines a ring handler that collects together
   a set of static files as one resource.

   The method of combining, and processing the bundle is left
   configurable.

   processor, the first argument, is a function that takes file or stream-like
   objects from your fetch function (resource by default) and returns an object
   suitable for a ring response :body, though an InputStream is recommended.

   The defaults use (comp clojure.java.io/file
                          clojure.java.io/resource) to load files,
   and a static (determined at namespace load time) timestamper. If you
   wish to use live timestamps, 'file-date will working on a seq of files.
   "
  ([name processor files]
     (bundle name processor files {}))
  
  ([name processor files
    {:keys [fetch
            timestamp]
     :or {fetch (comp io/file io/resource)
          timestamp static-timestamp}}]
     
     ;; A new ring handler
     (fn [{:keys [request-method
                 path-info
                 uri]
          :as req}]
       (when (and (= :get request-method)
                  (= (or path-info uri) name))
           (let [files (map fetch files)
                 latest (timestamp files)]
             (if (is-modified? latest
                               (get-in req [:headers "if-modified-since"]))
               ;; bundle has changed since cached, build new bundle
               (bundle-response files processor latest)
               
               ;; no change, just return a 304
               (-> (res/response "")
                   (res/status 304))))))))