import React, { useEffect, useMemo, useState } from "react";
import {
    ActivityIndicator,
    Alert,
    Linking,
    Platform,
    Pressable,
    ScrollView,
    StyleSheet,
    Text,
    View,
} from "react-native";
import { Ionicons } from "@expo/vector-icons";
import { useNavigation, useRoute, type RouteProp } from "@react-navigation/native";
import { SafeAreaView } from "react-native-safe-area-context";
import { isAxiosError } from "axios";

import MapPreview from "../components/MapPreview";
import { CSUN, distanceInMiles } from "../data/mapData";
import type { MapSpot } from "../data/mapData";
import { getDrinks } from "../services/drinks";
import { getLocationById } from "../services/locations";
import { useTheme, type Theme } from "../theme/ThemeProvider";
import type { Drink, Location } from "../types";

type CafeRoute = RouteProp<{ CafeDetails: { locationId: number } }, "CafeDetails">;

export default function CafeDetails() {
    const navigation = useNavigation<any>();
    const route = useRoute<CafeRoute>();
    const { theme } = useTheme();
    const styles = useMemo(() => makeStyles(theme), [theme]);
    const locationId = route.params?.locationId;

    const [cafe, setCafe] = useState<Location | null>(null);
    const [drinks, setDrinks] = useState<Drink[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<"not-found" | "network" | null>(null);
    const [retry, setRetry] = useState(0);

    useEffect(() => {
        let active = true;
        async function load() {
            setLoading(true);
            setError(null);
            setCafe(null);
            setDrinks([]);
            if (typeof locationId !== "number" || !Number.isInteger(locationId) || locationId <= 0) {
                setError("not-found");
                setLoading(false);
                return;
            }
            try {
                const [location, items] = await Promise.all([
                    getLocationById(locationId),
                    getDrinks({ locationId }),
                ]);
                if (active) {
                    setCafe(location);
                    setDrinks(items);
                }
            } catch (cause) {
                if (active) {
                    setError(isAxiosError(cause) && cause.response?.status === 404 ? "not-found" : "network");
                }
            } finally {
                if (active) setLoading(false);
            }
        }
        void load();
        return () => { active = false; };
    }, [locationId, retry]);

    const groups = useMemo(() => {
        const byCategory = new Map<string, Drink[]>();
        for (const drink of drinks) {
            const name = drink.category || "Other";
            const list = byCategory.get(name) ?? [];
            list.push(drink);
            byCategory.set(name, list);
        }
        return Array.from(byCategory.entries()).sort(([a], [b]) => a.localeCompare(b));
    }, [drinks]);

    const hasCoordinates = cafe &&
        Number.isFinite(cafe.latitude) && Number.isFinite(cafe.longitude);
    const mapSpot: MapSpot | null = hasCoordinates && cafe ? {
        id: cafe.id,
        name: cafe.name,
        latitude: cafe.latitude,
        longitude: cafe.longitude,
    } : null;
    const miles = mapSpot ? distanceInMiles(CSUN, mapSpot) : null;

    async function openDirections() {
        if (!mapSpot) return;
        const destination = `${mapSpot.latitude},${mapSpot.longitude}`;
        const url = Platform.OS === "ios"
            ? `http://maps.apple.com/?daddr=${destination}`
            : `https://www.google.com/maps/dir/?api=1&destination=${destination}`;
        try {
            await Linking.openURL(url);
        } catch {
            Alert.alert("Directions unavailable", "Please try again later.");
        }
    }

    return (
        <SafeAreaView style={styles.screen}>
            <ScrollView contentContainerStyle={styles.content}>
                <Pressable onPress={() => navigation.goBack()} style={styles.back}
                    accessibilityRole="button" accessibilityLabel="Back">
                    <Ionicons name="arrow-back" size={22} color={theme.text} />
                </Pressable>

                {loading ? (
                    <ActivityIndicator color={theme.icon} accessibilityLabel="Loading café details" />
                ) : error ? (
                    <View style={styles.messageArea}>
                        <Text style={styles.message} accessibilityRole="alert">
                            {error === "not-found" ? "Café not found." : "Unable to load café details."}
                        </Text>
                        {error === "network" && (
                            <Pressable onPress={() => setRetry((value) => value + 1)}>
                                <Text style={styles.retry}>Try again</Text>
                            </Pressable>
                        )}
                    </View>
                ) : cafe ? (
                    <>
                        <View style={styles.photo} accessibilityLabel="Café photo unavailable" />
                        <Text style={styles.title}>{cafe.name}</Text>
                        <Text style={styles.address}>{cafe.address}</Text>
                        {miles !== null && (
                            <Text style={styles.distance}>{miles.toFixed(1)} mi from CSUN</Text>
                        )}

                        <View style={styles.section}>
                            <Text style={styles.heading}>Location</Text>
                            {mapSpot ? (
                                <MapPreview
                                    key={cafe.id}
                                    spots={[mapSpot]}
                                    onPress={() => navigation.navigate("CafeMap", {
                                        spots: [mapSpot],
                                        focusOnSpot: true,
                                    })}
                                />
                            ) : (
                                <Text style={styles.address}>Map unavailable for this café.</Text>
                            )}
                        </View>

                        <View style={styles.section}>
                            <View style={styles.headingRow}>
                                <Text style={styles.heading}>Drinks at this café</Text>
                                <Text style={styles.count}>{drinks.length} {drinks.length === 1 ? "drink" : "drinks"}</Text>
                            </View>
                            {groups.length === 0 ? (
                                <Text style={styles.address}>No drinks listed for this café yet.</Text>
                            ) : groups.map(([category, items]) => (
                                <View key={category} style={styles.group}>
                                    <Text style={styles.groupTitle}>{category}</Text>
                                    {items.map((item) => (
                                        <Pressable key={item.id} style={styles.drink}
                                            onPress={() => navigation.navigate("DrinkDetails", { drinkId: item.id })}
                                            accessibilityRole="button"
                                            accessibilityLabel={`View ${item.name} details`}>
                                            <View style={styles.drinkPhoto} />
                                            <View style={styles.drinkInfo}>
                                                <Text style={styles.drinkName}>{item.name}</Text>
                                                {item.price != null && (
                                                    <Text style={styles.price}>${item.price.toFixed(2)}</Text>
                                                )}
                                            </View>
                                            <Ionicons name="chevron-forward" size={20} color={theme.icon} />
                                        </Pressable>
                                    ))}
                                </View>
                            ))}
                        </View>

                        {mapSpot && (
                            <Pressable style={styles.directions} onPress={() => { void openDirections(); }}>
                                <Text style={styles.directionsText}>Get Directions</Text>
                            </Pressable>
                        )}
                    </>
                ) : null}
            </ScrollView>
        </SafeAreaView>
    );
}

const makeStyles = (t: Theme) => StyleSheet.create({
    screen: { flex: 1, backgroundColor: t.background },
    content: { padding: 20, paddingBottom: 36 },
    back: {
        width: 36, height: 36, borderRadius: 18, backgroundColor: t.chip,
        alignItems: "center", justifyContent: "center", marginBottom: 12
    },
    photo: { width: "100%", aspectRatio: 1, backgroundColor: t.band, borderRadius: 8 },
    title: { color: t.text, fontSize: 24, fontWeight: "700", marginTop: 16 },
    address: { color: t.subtext, fontSize: 14, lineHeight: 21, marginTop: 5 },
    distance: {
        alignSelf: "flex-start", color: t.chipText, backgroundColor: t.chip,
        borderRadius: 20, paddingVertical: 9, paddingHorizontal: 14, fontWeight: "600", marginTop: 12
    },
    section: { marginTop: 22 },
    headingRow: { flexDirection: "row", alignItems: "baseline", justifyContent: "space-between" },
    heading: { color: t.text, fontSize: 16, fontWeight: "700", marginBottom: 10 },
    count: { color: t.subtext, fontSize: 12 },
    group: { marginTop: 14, gap: 9 },
    groupTitle: {
        color: t.subtext, fontSize: 13, fontWeight: "700", paddingBottom: 6,
        borderBottomWidth: StyleSheet.hairlineWidth, borderBottomColor: t.divider
    },
    drink: {
        flexDirection: "row", alignItems: "center", gap: 12, backgroundColor: t.band,
        borderRadius: 14, padding: 10
    },
    drinkPhoto: { width: 58, height: 58, borderRadius: 8, backgroundColor: t.chip },
    drinkInfo: { flex: 1 },
    drinkName: { color: t.text, fontSize: 15, fontWeight: "600" },
    price: { color: t.subtext, fontSize: 12, marginTop: 4 },
    directions: {
        backgroundColor: t.button, borderRadius: 26, padding: 15,
        alignItems: "center", marginTop: 25
    },
    directionsText: { color: t.buttonText, fontSize: 16, fontWeight: "700" },
    messageArea: { paddingVertical: 24 },
    message: { color: t.text, fontSize: 16 },
    retry: { color: t.button, fontSize: 16, fontWeight: "700", marginTop: 12 },
});
