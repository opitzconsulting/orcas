---
layout: page
title: Maintenance of the documentation
permalink: /docs/update-docs/
categories: 
- en
---

The documentation is created with [Jekyll](http://jekyllrb.com/) and is located in the own branch `gh-pages` within the normal GitHub repository.

## Dokumentation bearbeiten

In order to edit the documentation, you have to switch to this Branch.

Followed by the content can be edited directly. The actual content pages are located under `_docs`.
The navigation can be found under `_includes/navigation.html` and should be adapted if necessary.

## Work locally

To be able to see the result of local changes directly, you have to either Jekyll locally install (which is partially not easy), or start the Vagrant VM with `jekyll_vagrant`. The latter should ensure that immediately after a successful start, that the documentation is reachable under [http://localhost:4000](http://localhost:4000) (in fact directly on the host, and not only within the VM).

It should be noted that the Jekyll server starts through Vagrant provisioning, thus the shell command `vagrant up` never returns, but it shows directly the Jekyll server messages.

## Graphics

The existing graphics were created largely through ppts. The ppts lie in the directory `ppts` and the generated images in `assets`.