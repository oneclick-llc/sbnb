import {ColorValue, NativeModules, Platform, processColor} from "react-native";

const LINKING_ERROR =
  `The package 'react-native-sbnb' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const Sbnb = NativeModules.Sbnb
  ? NativeModules.Sbnb
  : new Proxy(
    {},
    {
      get() {
        throw new Error(LINKING_ERROR);
      },
    }
  );

export function setSystemUIColor(color: ColorValue) {
  if (Platform.OS === 'ios') return
  return Sbnb.setSystemUIColor(processColor(color))
}

export function setStatusBarStyle(dark: boolean) {
  if (Platform.OS === 'ios') return
  return Sbnb.setStatusBarStyle(dark)
}

export function setStatusBarHidden(hidden: boolean) {
  if (Platform.OS === 'ios') return
  return Sbnb.setStatusBarHidden(hidden)
}

export function toggleFitsSystemWindows(isEnabled: boolean) {
  if (Platform.OS === 'ios') return
  return Sbnb.toggleFitsSystemWindows(!isEnabled)
}

export function statusBarHeight(): number {
  if (Platform.OS === 'ios') return 0
  return Sbnb.statusBarHeight()
}

export function navigationBarHeight(): number {
  if (Platform.OS === 'ios') return 0
  return Sbnb.navigationBarHeight()
}
