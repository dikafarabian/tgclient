package com.exteragram.messenger.plugins;

/**
 * Шим строковых констант exteraGram. Значения — те же, что в
 * {@code com.exteragram.messenger.plugins.PluginsConstants} 12.9.0, а не наши
 * внутренние: плагин сравнивает их с тем, что приходит из SDK.
 */
public final class PluginsConstants {

    public static final String PYTHON = app.luminegram.plugins.PluginsConstants.PYTHON;
    public static final String SEND_MESSAGE_HOOK = app.luminegram.plugins.PluginsConstants.SEND_MESSAGE_HOOK;
    public static final String STRATEGY = app.luminegram.plugins.PluginsConstants.STRATEGY;
    public static final String PARAMS = app.luminegram.plugins.PluginsConstants.PARAMS;
    public static final String UPDATE = app.luminegram.plugins.PluginsConstants.UPDATE;
    public static final String UPDATES = app.luminegram.plugins.PluginsConstants.UPDATES;
    public static final String REQUEST = app.luminegram.plugins.PluginsConstants.REQUEST;
    public static final String RESPONSE = app.luminegram.plugins.PluginsConstants.RESPONSE;
    public static final String ERROR = app.luminegram.plugins.PluginsConstants.ERROR;
    public static final String PLUGINS = app.luminegram.plugins.PluginsConstants.PLUGINS;
    public static final String PLUGINS_EXT = app.luminegram.plugins.PluginsConstants.PLUGINS_EXT;
    public static final String PLUGINS_SDK = app.luminegram.plugins.PluginsConstants.PLUGINS_SDK;
    public static final String CREATE_SETTINGS = app.luminegram.plugins.PluginsConstants.CREATE_SETTINGS;
    public static final String APP_START = app.luminegram.plugins.PluginsConstants.APP_START;
    public static final String APP_STOP = app.luminegram.plugins.PluginsConstants.APP_STOP;
    public static final String APP_PAUSE = app.luminegram.plugins.PluginsConstants.APP_PAUSE;
    public static final String APP_RESUME = app.luminegram.plugins.PluginsConstants.APP_RESUME;
    public static final String ON_APP_EVENT = app.luminegram.plugins.PluginsConstants.ON_APP_EVENT;
    public static final String ON_PLUGIN_LOAD = app.luminegram.plugins.PluginsConstants.ON_PLUGIN_LOAD;
    public static final String ON_PLUGIN_UNLOAD = app.luminegram.plugins.PluginsConstants.ON_PLUGIN_UNLOAD;

    private PluginsConstants() {
    }

    public static final class Settings {
        public static final String TYPE = app.luminegram.plugins.PluginsConstants.Settings.TYPE;
        public static final String KEY = app.luminegram.plugins.PluginsConstants.Settings.KEY;
        public static final String TEXT = app.luminegram.plugins.PluginsConstants.Settings.TEXT;
        public static final String SUBTEXT = app.luminegram.plugins.PluginsConstants.Settings.SUBTEXT;
        public static final String ICON = app.luminegram.plugins.PluginsConstants.Settings.ICON;
        public static final String ACCENT = app.luminegram.plugins.PluginsConstants.Settings.ACCENT;
        public static final String RED = app.luminegram.plugins.PluginsConstants.Settings.RED;
        public static final String ON_CLICK = app.luminegram.plugins.PluginsConstants.Settings.ON_CLICK;
        public static final String DEFAULT = app.luminegram.plugins.PluginsConstants.Settings.DEFAULT;
        public static final String ITEMS = app.luminegram.plugins.PluginsConstants.Settings.ITEMS;
        public static final String HINT = app.luminegram.plugins.PluginsConstants.Settings.HINT;
        public static final String MULTILINE = app.luminegram.plugins.PluginsConstants.Settings.MULTILINE;
        public static final String MAX_LENGTH = app.luminegram.plugins.PluginsConstants.Settings.MAX_LENGTH;
        public static final String MASK = app.luminegram.plugins.PluginsConstants.Settings.MASK;
        public static final String ON_CHANGE = app.luminegram.plugins.PluginsConstants.Settings.ON_CHANGE;
        public static final String TYPE_SWITCH = app.luminegram.plugins.PluginsConstants.Settings.TYPE_SWITCH;
        public static final String TYPE_INPUT = app.luminegram.plugins.PluginsConstants.Settings.TYPE_INPUT;
        public static final String TYPE_SELECTOR = app.luminegram.plugins.PluginsConstants.Settings.TYPE_SELECTOR;
        public static final String TYPE_HEADER = app.luminegram.plugins.PluginsConstants.Settings.TYPE_HEADER;
        public static final String TYPE_DIVIDER = app.luminegram.plugins.PluginsConstants.Settings.TYPE_DIVIDER;
        public static final String TYPE_TEXT = app.luminegram.plugins.PluginsConstants.Settings.TYPE_TEXT;
        public static final String TYPE_EDIT_TEXT = app.luminegram.plugins.PluginsConstants.Settings.TYPE_EDIT_TEXT;
        public static final String TYPE_CUSTOM = app.luminegram.plugins.PluginsConstants.Settings.TYPE_CUSTOM;
        public static final String VIEW = app.luminegram.plugins.PluginsConstants.Settings.VIEW;
        public static final String ITEM = app.luminegram.plugins.PluginsConstants.Settings.ITEM;
        public static final String FACTORY = app.luminegram.plugins.PluginsConstants.Settings.FACTORY;
        public static final String FACTORY_ARGS = app.luminegram.plugins.PluginsConstants.Settings.FACTORY_ARGS;
        public static final String CREATE_SUB_FRAGMENT = app.luminegram.plugins.PluginsConstants.Settings.CREATE_SUB_FRAGMENT;
        public static final String ON_LONG_CLICK = app.luminegram.plugins.PluginsConstants.Settings.ON_LONG_CLICK;
        public static final String LINK_ALIAS = app.luminegram.plugins.PluginsConstants.Settings.LINK_ALIAS;

        private Settings() {
        }
    }

    public static final class Strategy {
        public static final String MODIFY = app.luminegram.plugins.PluginsConstants.Strategy.MODIFY;
        public static final String CANCEL = app.luminegram.plugins.PluginsConstants.Strategy.CANCEL;
        public static final String DEFAULT = app.luminegram.plugins.PluginsConstants.Strategy.DEFAULT;
        public static final String MODIFY_FINAL = app.luminegram.plugins.PluginsConstants.Strategy.MODIFY_FINAL;

        private Strategy() {
        }
    }

    public static final class Xposed {
        public static final String REPLACE_HOOKED_METHOD = app.luminegram.plugins.PluginsConstants.Xposed.REPLACE_HOOKED_METHOD;
        public static final String BEFORE_HOOKED_METHOD = app.luminegram.plugins.PluginsConstants.Xposed.BEFORE_HOOKED_METHOD;
        public static final String AFTER_HOOKED_METHOD = app.luminegram.plugins.PluginsConstants.Xposed.AFTER_HOOKED_METHOD;
        public static final String HOOK_FILTERS = app.luminegram.plugins.PluginsConstants.Xposed.HOOK_FILTERS;

        private Xposed() {
        }
    }

    public static final class DevServer {
        public static final String MODULE = app.luminegram.plugins.PluginsConstants.DevServer.MODULE;
        public static final String CLASS = app.luminegram.plugins.PluginsConstants.DevServer.CLASS;
        public static final String START_SERVER = app.luminegram.plugins.PluginsConstants.DevServer.START_SERVER;
        public static final String STOP_SERVER = app.luminegram.plugins.PluginsConstants.DevServer.STOP_SERVER;

        private DevServer() {
        }
    }

    public static final class MenuItemTypes {
        public static final String MESSAGE_CONTEXT_MENU = app.luminegram.plugins.PluginsConstants.MenuItemTypes.MESSAGE_CONTEXT_MENU;
        public static final String DRAWER_MENU = app.luminegram.plugins.PluginsConstants.MenuItemTypes.DRAWER_MENU;
        public static final String MAIN_MENU = app.luminegram.plugins.PluginsConstants.MenuItemTypes.MAIN_MENU;
        public static final String CHAT_ACTION_MENU = app.luminegram.plugins.PluginsConstants.MenuItemTypes.CHAT_ACTION_MENU;
        public static final String PROFILE_ACTION_MENU = app.luminegram.plugins.PluginsConstants.MenuItemTypes.PROFILE_ACTION_MENU;

        private MenuItemTypes() {
        }
    }

    public static final class MenuItemProperties {
        public static final String MENU_TYPE = app.luminegram.plugins.PluginsConstants.MenuItemProperties.MENU_TYPE;
        public static final String ITEM_ID = app.luminegram.plugins.PluginsConstants.MenuItemProperties.ITEM_ID;
        public static final String TEXT = app.luminegram.plugins.PluginsConstants.MenuItemProperties.TEXT;
        public static final String SUBTEXT = app.luminegram.plugins.PluginsConstants.MenuItemProperties.SUBTEXT;
        public static final String ICON = app.luminegram.plugins.PluginsConstants.MenuItemProperties.ICON;
        public static final String ON_CLICK = app.luminegram.plugins.PluginsConstants.MenuItemProperties.ON_CLICK;
        public static final String CONDITION = app.luminegram.plugins.PluginsConstants.MenuItemProperties.CONDITION;
        public static final String PRIORITY = app.luminegram.plugins.PluginsConstants.MenuItemProperties.PRIORITY;

        private MenuItemProperties() {
        }
    }

    public static final class HookFilterTypes {
        public static final String RESULT_IS_NULL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_IS_NULL;
        public static final String RESULT_IS_TRUE = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_IS_TRUE;
        public static final String RESULT_IS_FALSE = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_IS_FALSE;
        public static final String RESULT_NOT_NULL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_NOT_NULL;
        public static final String RESULT_IS_INSTANCE_OF = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_IS_INSTANCE_OF;
        public static final String RESULT_EQUAL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_EQUAL;
        public static final String RESULT_NOT_EQUAL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.RESULT_NOT_EQUAL;
        public static final String ARGUMENT_IS_NULL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_IS_NULL;
        public static final String ARGUMENT_IS_TRUE = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_IS_TRUE;
        public static final String ARGUMENT_IS_FALSE = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_IS_FALSE;
        public static final String ARGUMENT_NOT_NULL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_NOT_NULL;
        public static final String ARGUMENT_IS_INSTANCE_OF = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_IS_INSTANCE_OF;
        public static final String ARGUMENT_EQUAL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_EQUAL;
        public static final String ARGUMENT_NOT_EQUAL = app.luminegram.plugins.PluginsConstants.HookFilterTypes.ARGUMENT_NOT_EQUAL;
        public static final String CONDITION = app.luminegram.plugins.PluginsConstants.HookFilterTypes.CONDITION;
        public static final String OR = app.luminegram.plugins.PluginsConstants.HookFilterTypes.OR;

        private HookFilterTypes() {
        }
    }
}
