---
layout: docs
title: Projekt-Struktur
permalink: /docs/project-structure/
prev_section:
next_section: examples
---

#Projekt-Struktur

Orcas hat folgende Hauptverzeichnisse:

- examples
- orcas_core
- orcas_dbdoc
- orcas_domain_extension
- orcas_extensions
- orcas_integrationstest
- orcas_sqlplus_core
- orcas_vagrant

Unter **examples** befinden sich eine Reihe von Beispielprojekten die die Nutzung von Orcas zeigen. Hier kann man sich ein [Beispielprojekt]({{site.baseurl}}/docs/examples/) aussuchen und als Kopiervorlage für das eigene Projekt verwenden.

Unter **orcas_core** liegt der Hauptbestandsteil von Orcas. Dieses Verzeichnis wird benötigt um Orcas laufen zu lassen. Das Verzeichnis kann aus einem konkreten Projekt heraus referenziert oder in ein Projekt kopiert werden. Der Inhalt des Verzeichnisses darf jedoch nicht geändert werden. Sollten solche Änderungen gewünscht sein, bitte immer mit den Entwicklern von Orcas [abstimmen](https://github.com/opitzconsulting/orcas/issues), da mit großer Wahrscheinlichkeit einer der folgenden Gründe vorliegt:

- Es gibt ein Verständnisproblem und die gewünschte Funktionalität kann auf einem anderen, dafür vorgesehenen, Weg erreicht werden.
- Es gibt einen Bug, dieser sollte in Orcas behoben werden.
- Es wird eine Erweiterung benötigt, diese sollte entweder in Orcas aufgenommen werden oder projektspezifisch, dann aber in einem anderen projekteigenen Verzeichnis.

Unter **orcas_dbdoc** befindet sich das Tool [dbdoc]({{site.baseurl}}/docs/dbdoc/). Mit dbdoc lässt sich eine grafische Darstellung der Datenbankschemata erstellen.

Unter **orcas_domain_extension** befinden sich die [Domain-Extensions]({{site.baseurl}}/docs/extensions/). Die Domain-Extension bietet eine Art Templating-Mechanismus. Damit ist es einfach möglich Standard-Spalten für Tabellen zu erzeugen (z.B. ID oder version). Dabei besteht auch die Möglichkeit PK-, FK-, UK- und Check-Constraints zu generieren.

Unter **orcas_extensions** befinden sich beispiel [Extensions]({{site.baseurl}}/docs/extensions/). Mit eigenen Extensions kann man Orcas sehr stark erweitern. In dem Verzeichnis befinden sich Beispiele, die man ins eigene Projekt kopiert kann um sie dort ggf. anzupassen.

Unter **orcas_integrationstest** ist der [Integrationstest]({{site.baseurl}}/docs/integration-tests/) abgelegt. Dieser überprüft die korrekte Arbeitsweise von Orcas durch automatisch gesteuerte Testabläufe auf verschiedenen Datenbanksystemen. Der Integrationstest kann auch lokal ausgeführt und um eigene Tests erweitert werden.

Unter **orcas_sqlplus_core** liegt eine API, die zum dirketen Aufruf aus SQL\*Plus  heraus vorgesehen ist. Diese Möglichkeit ist für Alt-Projekte vorgesehen und sollte bei neuen Projekten nicht mehr eingesetzt werden.

Unter **orcas_vagrant** liegt eine Vagrant-Konfiguration mit der man eine VM konfigurieren und starten kann, in der Orcas direkt lauffähig ist.
