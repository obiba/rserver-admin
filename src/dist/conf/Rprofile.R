# install R libraries in a R version independent directory
if (!file.exists("/var/lib/rserver/R/library")) {
  dir.create("/var/lib/rserver/R/library", recursive=TRUE)
}
.libPaths("/var/lib/rserver/R/library")
# for Pandoc
Sys.setenv(HOME="/var/lib/rserver")
# newline required