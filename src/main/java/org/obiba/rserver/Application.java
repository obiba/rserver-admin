/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.rserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableConfigurationProperties(RProperties.class)
public class Application {

  public static void main(String... args) throws Exception {
    ConfigFileApplicationListener listener = new ConfigFileApplicationListener();
    listener.setSearchLocations("classpath:,file:" + Resources.getRServerHomeDir() + "/conf/");

    SpringApplication springApp = new SpringApplication(Application.class);
    springApp.addListeners(listener);
    springApp.run(args);
  }

}