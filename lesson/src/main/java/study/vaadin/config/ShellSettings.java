package study.vaadin.config;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.theme.Theme;

@Push
@Theme("vaadin-study")
public class ShellSettings implements AppShellConfigurator {
}
