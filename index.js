import React from "react";
import{
  AppRegistry,
  StyleSheet,
  Text,
  Button,
  View,
  NativeModules,
  Alert
} from "react-native";
// import Constants from "expo-constants";
var test = NativeModules.JumpModule;
class WelcomeGuide extends React.Component{
	render(){
		return(
				<View style={styles.container}>
					<Button title="Start Now!" onPress={this.scanBluetooth}/>
				</View>
			);
	}

	scanBluetooth(){
		test.jump();
	}
}  

const styles = StyleSheet.create({
	container:{
		flex:1,
		justifyContent:'center'
	},
	hello:{
		fontSize:20,
		textAlign:'center',
		margin:10
	},
	fixToRow:{
		flexDirection:'row',
		justifyContent:'space-between',
		marginTop:20,
		marginHorizontal:16,
	}
});

AppRegistry.registerComponent(
	'DspreadReactPosApp',
	() => WelcomeGuide
);