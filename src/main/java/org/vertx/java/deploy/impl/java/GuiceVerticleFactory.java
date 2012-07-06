package org.vertx.java.deploy.impl.java;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.vertx.java.deploy.Verticle;
import org.vertx.java.deploy.VerticleFactory;
import org.vertx.java.deploy.impl.VerticleManager;

public class GuiceVerticleFactory implements VerticleFactory{

    private static final String GUICE_PREFIX = "guiceModule:";

    private VerticleManager mgr;

    @Override
    public void init(VerticleManager manager) {
        this.mgr = manager;
    }

    @Override
    public String getLanguage() {
        return "guice";
    }

    @Override
    public boolean isFactoryFor(String main) {
        return main.startsWith(GUICE_PREFIX);
    }

    @Override
    public Verticle createVerticle(String main, ClassLoader parentCL) throws Exception {
        String moduleClass = main.substring(GUICE_PREFIX.length());
        Injector injector = Guice.createInjector(Class.forName(moduleClass).asSubclass(Module.class).newInstance());
        Verticle verticle = injector.getInstance(Verticle.class);
        return verticle;
    }

    @Override
    public void reportException(Throwable t) {
        mgr.getLogger().error("Exception in Java verticle script", t);
    }
}

