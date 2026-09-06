module fr.ensibs.robots.app {
    requires transitive fr.ensibs.robots.api;
    requires transitive java.desktop;
    exports fr.ensibs.robots;
    exports fr.ensibs.robots.impl;
}
