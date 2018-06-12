# RServer Admin [![Build Status](https://travis-ci.org/obiba/rserver-admin.svg?branch=master)](https://travis-ci.org/obiba/rserver-admin)

R server REST controller: a REST server that starts/stops a R server + returns the R server connection info.

Requires [R](http://www.r-project.org/) to be installed with [Rserve](http://rforge.net/Rserve/) package.

* Have a bug or a question? Please create an issue on [GitHub](https://github.com/obiba/rserver-admin/issues).
* Continuous integration is on [Travis](https://travis-ci.org/obiba/rserver-admin).

## Usage

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

## Mailing list

Have a question? Ask on our mailing list!

obiba-users@googlegroups.com

[http://groups.google.com/group/obiba-users](http://groups.google.com/group/obiba-users)

## License

OBiBa software are open source and made available under the [GPL3 licence](http://www.obiba.org/pages/license/). OBiBa software are free of charge.

# OBiBa acknowledgments

If you are using OBiBa software, please cite our work in your code, websites, publications or reports.

"The work presented herein was made possible using the OBiBa suite (www.obiba.org), a  software suite developed by Maelstrom Research (www.maelstrom-research.org)"
