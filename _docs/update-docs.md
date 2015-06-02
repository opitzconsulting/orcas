---
layout: page
title: Pflege der Dokumentation
permalink: /docs/update-docs/
---

Die Dokumentation wird mit [Jekyll](http://jekyllrb.com/) erstellt und liegt in einem eigenen Branch `gh-pages` im normalen GitHub-Repository.

## Dokumentation bearbeiten

Um die Dokumentation bearbeiten zu können, muss man auf diesen Branch wechseln.

Anschließend können die Inhalte direkt bearbeitet werden. Die eigentlichen Inhaltsseiten liegen unter `_docs`.
Die Navigation ist unter `_includes/navigation.html` zu finden und ggf. anzupassen.

## Lokal arbeiten

Um das Ergebnis der lokalen Änderungen direkt sehen zu können, muss man entweder Jekyll lokal installieren (das ist teilweise nicht ganz einfach), oder die Vagrant VM unter `jekyll_vagrant` starten. Letzteres sollte dafür sorgen, dass direkt nach erfolgreichem Start die Dokumentation unter [http://localhost:4000/orcas](http://localhost:4000/orcas) erreichbar ist (und zwar direkt auf dem host und nicht nur innerhalb der VM).

Zu beachten ist, dass der Jekyll-Server über Vagrant-Provisioning gestartet wird, somit kommt der Kommandozeilen-Befehl `vagrant up` nie zurück, zeigt dafür aber direkt die jekyll-server-Meldungen an.

## Grafiken

Die vorhandenen Grafiken wurden weitestgehend über ppt's erstellt. Die ppt's liegen unter dem Pfad `ppts` und die erzeugten Bild-Dateien unter `assets`.
