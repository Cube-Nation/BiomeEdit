#!/usr/bin/env python
import sys
import os
import os.path
import stat
import xml.dom.minidom

if os.environ["TRAVIS_SECURE_ENV_VARS"] == "false":
  print "no secure env vars available, skipping deployment"
  sys.exit()

homedir = os.path.expanduser("~")

m2 = xml.dom.minidom.parse(homedir + '/.m2/settings.xml')
settings = m2.getElementsByTagName("settings")[0]

serversNodes = settings.getElementsByTagName("servers")
if not serversNodes:
  serversNode = m2.createElement("servers")
  settings.appendChild(serversNode)
else:
  serversNode = serversNodes[0]


# release server
sonatypeServerNode = m2.createElement("server")
sonatypeServerId = m2.createElement("id")
sonatypeServerUser = m2.createElement("username")
sonatypeServerPass = m2.createElement("password")

idNode = m2.createTextNode(os.environ["SONATYPE_RELEASE_SERVER"])
userNode = m2.createTextNode(os.environ["SONATYPE_USERNAME"])
passNode = m2.createTextNode(os.environ["SONATYPE_PASSWORD"])

sonatypeServerId.appendChild(idNode)
sonatypeServerUser.appendChild(userNode)
sonatypeServerPass.appendChild(passNode)

sonatypeServerNode.appendChild(sonatypeServerId)
sonatypeServerNode.appendChild(sonatypeServerUser)
sonatypeServerNode.appendChild(sonatypeServerPass)

serversNode.appendChild(sonatypeServerNode)


# snapshot server
_sonatypeServerNode = m2.createElement("server")
_sonatypeServerId = m2.createElement("id")
_sonatypeServerUser = m2.createElement("username")
_sonatypeServerPass = m2.createElement("password")

_idNode = m2.createTextNode(os.environ["SONATYPE_SNAPSHOT_SERVER"])
_userNode = m2.createTextNode(os.environ["SONATYPE_USERNAME"])
_passNode = m2.createTextNode(os.environ["SONATYPE_PASSWORD"])

_sonatypeServerId.appendChild(_idNode)
_sonatypeServerUser.appendChild(_userNode)
_sonatypeServerPass.appendChild(_passNode)

_sonatypeServerNode.appendChild(_sonatypeServerId)
_sonatypeServerNode.appendChild(_sonatypeServerUser)
_sonatypeServerNode.appendChild(_sonatypeServerPass)

serversNode.appendChild(_sonatypeServerNode)

m2Str = m2.toxml()

f = open(homedir + '/.m2/mySettings.xml', 'w')
f.write(m2Str)
f.close()
