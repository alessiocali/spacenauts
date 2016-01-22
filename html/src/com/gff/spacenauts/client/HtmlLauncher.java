package com.gff.spacenauts.client;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.gwt.GwtApplication;
import com.badlogic.gdx.backends.gwt.GwtApplicationConfiguration;
import com.gff.spacenauts.Spacenauts;

public class HtmlLauncher extends GwtApplication {

        @Override
        public GwtApplicationConfiguration getConfig () {
                return new GwtApplicationConfiguration(342, 602);
        }

        @Override
        public ApplicationListener getApplicationListener () {
                return new Spacenauts();
        }
}