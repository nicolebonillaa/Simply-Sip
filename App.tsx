/**
 * Simply Sip App
 *
 * @format
 */

import React from 'react';
import { View, Text, StyleSheet } from 'react-native';

function App(): React.JSX.Element {
  return (
    <View style={styles.container}>
      <Text style={styles.title}>Simply Sip</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },

  title: {
    fontSize: 28,
    fontWeight: 'bold',
  },
});

export default App;
