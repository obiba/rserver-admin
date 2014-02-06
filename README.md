rserver-admin
=============

R server REST controller: a REST server that starts/stops a R server + returns the R server connection info.

Requires [R](http://www.r-project.org/) to be installed with [Rserve](http://rforge.net/Rserve/) package.

On debian systems, R and Rserve can be installed via `apt`:

```
sudo apt-get install r-base r-cran-rserve
```

### Start server

```
make all launch
```

### Test server

Requires `curl` and running server.

```
make test
```
