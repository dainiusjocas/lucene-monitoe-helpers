.PHONY: release
release:
	rm release.properties || true
	rm pom.xml.releaseBackup || true
	mvn release:prepare
