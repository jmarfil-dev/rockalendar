import Aura from "@primeuix/themes/aura";
import { definePreset } from "@primeuix/themes";

// Preset en pruebas
export const RockalendarPreset = definePreset(Aura, {
  semantic: {
    primary: {
      50: "{purple.50}",
      100: "{purple.100}",
      200: "{purple.200}",
      300: "{purple.300}",
      400: "{purple.400}",
      500: "{purple.500}",
      600: "{purple.600}",
      700: "{purple.700}",
      800: "{purple.800}",
      900: "{purple.900}",
      950: "{purple.950}",
    },
    success: {
      50: "{indigo.50}",
      100: "{indigo.100}",
      200: "{indigo.200}",
      300: "{indigo.300}",
      400: "{indigo.400}",
      500: "{indigo.500}",
      600: "{indigo.600}",
      700: "{indigo.700}",
      800: "{indigo.800}",
      900: "{indigo.900}",
      950: "{indigo.950}",
    },
    info: {
      50: "{lime.50}",
      100: "{lime.100}",
      200: "{lime.200}",
      300: "{lime.300}",
      400: "{lime.400}",
      500: "{lime.500}",
      600: "{lime.600}",
      700: "{lime.700}",
      800: "{lime.800}",
      900: "{lime.900}",
      950: "{lime.950}",
    },
    warn: {
      50: "{orange.50}",
      100: "{orange.100}",
      200: "{orange.200}",
      300: "{orange.300}",
      400: "{orange.400}",
      500: "{orange.500}",
      600: "{orange.600}",
      700: "{orange.700}",
      800: "{orange.800}",
      900: "{orange.900}",
      950: "{orange.950}",
    },
    error: {
      50: "{red.50}",
      100: "{red.100}",
      200: "{red.200}",
      300: "{red.300}",
      400: "{red.400}",
      500: "{red.500}",
      600: "{red.600}",
      700: "{red.700}",
      800: "{red.800}",
      900: "{red.900}",
      950: "{red.950}",
    },
    secondary: {
      50: "{red.50}",
      100: "{red.100}",
      200: "{red.200}",
      300: "{red.300}",
      400: "{red.400}",
      500: "{red.500}",
      600: "{red.600}",
      700: "{red.700}",
      800: "{red.800}",
      900: "{red.900}",
      950: "{red.950}",
    },
    contrast: {
      0: "#ffffff",
      900: "#000000",
    },
    colorScheme: {
      dark: {
        surface: {
          0: "#ffffff",
          50: "{zinc.50}",
          100: "{zinc.100}",
          200: "{zinc.200}",
          300: "{zinc.300}",
          400: "{zinc.400}",
          500: "{zinc.500}",
          600: "{zinc.600}",
          700: "{zinc.700}",
          800: "{zinc.800}",
          900: "{zinc.900}",
          950: "{zinc.950}",
        },
        formField: {
          background: "{surface.800}",
          //color: "{secondary.500}",
          //placeholderColor: "{secondary.400}",
          //iconColor: "{secondary.400}",
        },
        //list: {
        //option: {
        //focusBackground: "{primary.950}",
        //selectedBackground: "{primary.900}",
        //selectedFocusBackground: "{primary.800}",
        //color: "{secondary.500}",
        //focusColor: "{primary.500}",
        //selectedColor: "{secondary.500}",
        //selectedFocusColor: "{primary.500}",
        //icon: {
        //color: "{secondary.400}",
        //focusColor: "{secondary.500}",
        //},
        //},
        //},
        //text: {
        // color: "{primary.500}",
        //mutedColor: "{secondary.500}", // text-color-secondary
        //hoverMutedColor: "{secondary.900}",
        //},
      },
    },
  },
  components: {
    message: {
      colorScheme: {
        dark: {
          success: {
            background: "{success.950}",
            borderColor: "{success.800}",
            color: "{success.300}",
          },
          info: {
            background: "{info.950}",
            borderColor: "{info.800}",
            color: "{info.300}",
          },
          warn: {
            background: "{warn.950}",
            borderColor: "{warn.800}",
            color: "{warn.300}",
          },
          error: {
            background: "{error.950}",
            borderColor: "{error.800}",
            color: "{error.300}",
          },
          secondary: {
            background: "{secondary.950}",
            borderColor: "{secondary.800}",
            color: "{secondary.300}",
          },
        },
      },
    },
    tag: {
      colorScheme: {
        dark: {
          info: {
            background: "{info.950}",
            color: "{info.300}",
          },
        },
      },
    },
  },
});
