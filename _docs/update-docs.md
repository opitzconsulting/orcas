---
layout: page
title: Pflege der Dokumentation
permalink: /docs/update-docs/
---
Die Dokumentation wird mit jekyll erstellt.
Sie liegt in einem eigenen Branch(gh-pages) im normalen github-repo.

##Dokumentation bearbeiten
Um die Dokumentation bearbeiten zu können muss man auf diesen Branch wechseln.

Anschließend können die Inhalte direkt bearbeitet werden. Die Hauptseiten liegen unter _docs.
Die Navigation ist unter _includes/navigation.html zu finden.

##Lokal arbeiten
Um das Ergebnis der lokalen Änderungen direkt sehen zu können muss man entweder jekyll lokal installieren (ist teilweise nicht ganz einfach), oder die Vagrant VM unter jekyll_vagrant starten. Letzteres sollte dafür sorgen, das dierkt nach erfolgreichem Start die Dokumentation unter http://localhost:4000/ erreichbar ist (und zwar direkt auf dem host und nichzt nur innerhalb der VM). Zu beachten ist, dass der jekyll-server über Vagrant-Provisioning gestartet wird, somit kommt der Kommandozeilen-Befehl "vagrant up" nie zurück, zeigt dafür aber direkt die jekyll-server-Meldungen an.

##Grafiken
Die vorhandenen Grafiken wurden weitestgehend über ppt's erstellt. Die ppt's liegen unter ppts und die erzeugten Bild-Dateien unter assets.   

