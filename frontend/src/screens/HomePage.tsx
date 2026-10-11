import React from "react";
import { View, StyleSheet, Text, TextInput, Image, Button, Pressable, FlatList, ScrollView} from "react-native";
import {SafeAreaView} from "react-native-safe-area-context";
import {useState} from "react";
import {useNavigation} from "@react-navigation/native";
import {drinks, stores} from "../fakeDrinks.ts";


export default function HomePage() {
    const[search, setSearch] = useState("");
    const navigation = useNavigation();
    
    
    return (
            <SafeAreaView style = {styles.container}>
            <ScrollView showsVerticalScrollIndicator={false}>
            <View style = {styles.welcomeRow}>
                <Image
                style={styles.tinyLogo}
                source={require(
                    "../../assets/simply sip logo/app-logo.png")}
                />
                
                <Text style={styles.welcomeTitle}>
                        Welcome, Guest!
                </Text>
            </View>
                
            <View style={styles.searchBox}>
                <TextInput
                    placeholder = "Search drinks or places..."
                    onChangeText={setSearch}
                    value = {search}
                    style = {styles.input}
                />
                <Pressable
                    style={styles.filterButton}
                    onPress={() => navigation.navigate("Filters")}
                    accessibilityLabel="Filters"
                    >
                        <Image
                        style={styles.filterIcon}
                        source={require("../../assets/filter.png")}
                    />
                </Pressable>
        </View>
    <Text style={styles.popDrinksTitle}>
            Popular Drinks in Your Area</Text>
    <FlatList
    data={drinks}
    renderItem={({item}) =>
        (
         <View style={styles.card}>
         <View style={styles.imagePlaceholder} />
         <Text style={styles.name}>{item.name}</Text>
             <Text style={styles.place}>{item.place}</Text>
             </View>
         )}
        keyExtractor={(item) => item.id}
        horizontal
        showsHorizontalScrollIndicator={false}
    />
    <Text style={styles.popStoresTitle}>
                    Popular Stores in Your Area</Text>
    <FlatList
        data={stores}
        renderItem={({item}) =>
            (
             <View style={styles.card}>
             <View style={styles.imagePlaceholder} />
             <Text style={styles.name}>{item.name}</Text>
                 </View>
             )}
            keyExtractor={(item) => "store-" + item.id}
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.carouselContainer}
        />
            
</ScrollView>
</SafeAreaView>
);
    
}

const styles = StyleSheet.create({
  container: { flex: 1, paddingHorizontal: 1 },
    searchRow:
    {
        flexDirection: "row",
        alignItems: "center",
        gap: 5,
        marginTop: 3,
    },
    welcomeRow:
    {
        flexDirection: "row",
        alignItems: "center",
        paddingHorizontal: 15,
        marginBottom: 10,
    },
    tinyLogo:
    {
        width: 70,
        height: 70,
    },
    input: {
        flex: 1,
        padding: 12,
        height: "100%",
        fontSize: 12,
    },
    searchBox:
    {
        flexDirection: "row",
        alignItems: "center",
        borderWidth: 1,
        borderRadius: 8,
        paddingHorizontal: 8,
        marginRight: 12,
        marginBottom: 15,
        marginLeft: 12,
        height: 45,
        
    },
    filterButton:
    {
        width: 10,
        alignItems: "center",
        justifyContent: "center",
        marginRight: 10,
    },
    filterIcon:
    {
        width: 18,
        height: 18,
        resizeMode: "contain",
    },
    
    card:
    {
        backgroundColor: "#fff",
        padding: 12,
        marginLeft: 15,
        borderRadius: 12,
        width: 150,
        alignSelf: 'flex-start',
    },
    imagePlaceholder:
    {
        width:"100%",
        height: 100,
        backgroundColor: "#E0E0E0",
        borderRadius: 6,
        marginBottom: 8,
    },
    name:
    {
        fontSize: 16,
        fontWeight: "bold",
    },
    place:
    {
        fontSize: 14,
        color: "#666",
        marginBottom: 4,
    },
    popDrinksTitle:
    {
        fontSize: 18,
        fontWeight: "bold",
        marginLeft: 15,
        marginBottom: 10,
        marginTop: 15,
        color: "#333",
    },
    welcomeTitle:
    {
        fontSize: 22,
        fontWeight: "bold",
        marginLeft: 15,
        marginBottom: 10,
        marginTop: 15,
        color: "#333",
        alignSelf: "center",
    },
    popStoresTitle:
    {
        fontSize: 18,
        fontWeight: "bold",
        marginLeft: 15,
        marginBottom: 10,
        marginTop: 15,
        color: "#333",
    },
    carouselContainer:
    {
        paddingRight: 15,
        paddingBottom: 15,
    }
        
    
});




