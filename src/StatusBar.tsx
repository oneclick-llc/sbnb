import React, {useCallback, useEffect, useRef} from "react";
import {Platform} from "react-native";
import {setStatusBarStyle, setSystemUIColor, toggleFitsSystemWindows as _toggleFitsSystemWindows} from "./index";
// @ts-ignore
import NativeStatusBarManagerIOS from 'react-native/Libraries/Components/StatusBar/NativeStatusBarManagerIOS'


function createStackEntry(props: any): any {
  return {
    backgroundColor: props.backgroundColor,
    barStyle: props.barStyle,
    translucent: props.translucent,
    hidden: props.hidden,
  }
}

function mergePropsStack(
  propsStack: Array<any>,
  defaultValues: any,
): any {
  return propsStack.reduce(
    (prev, cur) => {
      for (const prop in cur) {
        // @ts-ignore
        if (cur[prop] != null) {
          // @ts-ignore
          prev[prop] = cur[prop];
        }
      }
      return prev;
    },
    {...defaultValues},
  );
}

export interface StatusBarProps {
  backgroundColor?: string;
  translucent?: boolean;
  barStyle?: ('default' | 'light-content' | 'dark-content'),
  changeOnFocus?: boolean
}

const _propsStack: Array<any> = []

let _updateImmediate: any | undefined
let _currentValues: {
  hidden: boolean;
  translucent: boolean;
  barStyle:('default' | 'light-content' | 'dark-content')
} | null = null

let _defaultProps: any = {
  backgroundColor: 'black',
  barStyle: 'default',
  translucent: false,
  hidden: false,
}

function toggleFitsSystemWindows(isEnabled: boolean) {
  if (Platform.OS !== 'android') return
  _defaultProps.translucent = isEnabled
  _toggleFitsSystemWindows(isEnabled)
}

function pushStackEntry(props: any): any {
  const entry = createStackEntry(props)
  _propsStack.push(entry)
  _updatePropsStack()
  return entry
}

function popStackEntry(entry: any) {
  const index = _propsStack.indexOf(entry)
  if (index !== -1) _propsStack.splice(index, 1)
  _updatePropsStack()
}

function replaceStackEntry(entry: any, props: any): any {
  const newEntry = createStackEntry(props)
  const index = _propsStack.indexOf(entry)
  if (index !== -1) _propsStack[index] = newEntry

  _updatePropsStack()
  return newEntry
}

function _updatePropsStack() {
  // Send the update to the native module only once at the end of the frame.
  clearImmediate(_updateImmediate)
  _updateImmediate = setImmediate(() => {
    const oldProps = _currentValues
    const mergedProps = mergePropsStack(_propsStack, _defaultProps)



    // Update the props that have changed using the merged values from the props stack.
    if (Platform.OS === 'ios') {
      if (!oldProps || oldProps.barStyle !== mergedProps.barStyle) {
        NativeStatusBarManagerIOS.setStyle(mergedProps.barStyle, false,);
      }
    } else if (Platform.OS === 'android') {
      if (mergedProps.backgroundColor == null) {
      } else {
        setSystemUIColor(mergedProps.backgroundColor)
      }
      setStatusBarStyle(mergedProps.barStyle === 'dark-content')

      if (
        !oldProps ||
        oldProps.translucent !== mergedProps.translucent ||
        mergedProps.translucent
      ) {
        toggleFitsSystemWindows(mergedProps.translucent)
      }
    }
    // Update the current prop values.
    _currentValues = mergedProps
  })
}


export function StatusBar(props: StatusBarProps) {
  const _stackEntry = useRef<any>()

  try {
    if (props.changeOnFocus) {
      require('@react-navigation/native').useFocusEffect(useCallback(() => {
        _stackEntry.current = pushStackEntry(props)
        return () => {
          popStackEntry(_stackEntry.current)
        }
      }, []))
    }
  } catch (e) {
  }

  useEffect(() => {
    if (props.changeOnFocus) return
    _stackEntry.current = pushStackEntry(props)
    return () => popStackEntry(_stackEntry.current)
  }, [])

  useEffect(() => {
    if (!_stackEntry.current) return
    _stackEntry.current = replaceStackEntry(_stackEntry.current, props)
  }, [props])

  return null
}
