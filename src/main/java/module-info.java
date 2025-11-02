module fr.ensibs.robots.api
{
    // public packages exported
    exports fr.ensibs.robots.logic;
    exports fr.ensibs.robots.view;
    exports fr.ensibs.robots.factories;

    // java.awt and java.swing: transitive as dependent modules should use it
    requires transitive java.desktop;
}
