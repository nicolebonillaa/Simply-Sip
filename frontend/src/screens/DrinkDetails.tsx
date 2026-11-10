//  DrinkDetails.tsx
//  Created by Faith Hill on 10/22/26.

import React, { useEffect, useMemo, useState } from "react";
import {
    ActivityIndicator, Alert, Linking, Platform, Pressable,
    ScrollView, StyleSheet, Text, TextInput, View
} from "react-native";

import { Theme, useTheme } from "../theme/ThemeProvider";
import { useRoute, useNavigation, type RouteProp } from "@react-navigation/native";
import { SafeAreaView } from "react-native-safe-area-context";
import { Ionicons } from "@expo/vector-icons";
import { isAxiosError } from "axios";
import { CSUN, distanceInMiles } from "../data/mapData";
import { getDrinkById } from "../services/drinks";
import type { Drink } from "../types";

type DrinkDetailsRoute = RouteProp<{ DrinkDetails: { drinkId: number } }, "DrinkDetails">;
type DraftReview = { rating: number; comment: string };
type Review = DraftReview & { id: string; name: string; date: string; order: number };
type ReviewSort = "recent" | "highest" | "lowest";

// Design examples only. The backend does not have a reviews endpoint yet.
const SAMPLE_REVIEWS: Review[] = [
    {
        id: "sample-1", name: "Alex M.", date: "Oct 30", order: 4, rating: 5,
        comment: "Smooth and not too sweet. I would get it again."
    },
    {
        id: "sample-2", name: "Jordan P.", date: "Oct 27", order: 3, rating: 4,
        comment: "Nice flavor, though I wish the coffee tasted a little stronger."
    },
    {
        id: "sample-3", name: "Sam R.", date: "Oct 23", order: 2, rating: 5,
        comment: "A good afternoon pick-me-up. The oat milk option was great."
    },
    {
        id: "sample-4", name: "Taylor K.", date: "Oct 18", order: 1, rating: 3,
        comment: "Good drink, but sweeter than I expected."
    },
];

export default function DrinkDetails() {
    const { theme } = useTheme();
    const styles = useMemo(() => makeStyles(theme), [theme]);
    const [isLiked, setIsLiked] = useState(false);
    const [rating, setRating] = useState(0);
    const [selectedMilk, setSelectedMilk] = useState<string | null>(null);
    const [selectedSizeId, setSelectedSizeId] = useState<number | null>(null);
    const [showNutrition, setShowNutrition] = useState(false);
    const [comment, setComment] = useState("");
    const [draftReview, setDraftReview] = useState<DraftReview | null>(null);
    const [reviewSort, setReviewSort] = useState<ReviewSort>("recent");
    const [visibleReviews, setVisibleReviews] = useState(2);
    const [pressedDrink, setPressedDrink] = useState<Drink | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<"not-found" | "network" | null>(null);
    const [retry, setRetry] = useState(0);

    const route = useRoute<DrinkDetailsRoute>();
    const navigation = useNavigation<any>();
    const drinkId = route.params?.drinkId;

    useEffect(() => {
        let active = true;

        async function load() {
            setLoading(true);
            setError(null);
            setPressedDrink(null);
            setRating(0);
            setSelectedMilk(null);
            setSelectedSizeId(null);
            setShowNutrition(false);
            setComment("");
            setDraftReview(null);
            setReviewSort("recent");
            setVisibleReviews(2);
            setIsLiked(false);

            if (typeof drinkId !== "number" || !Number.isInteger(drinkId) || drinkId <= 0) {
                setError("not-found");
                setLoading(false);
                return;
            }

            try {
                const drink = await getDrinkById(drinkId);
                if (active) setPressedDrink(drink);
            } catch (cause) {
                if (active) {
                    setError(
                        isAxiosError(cause) && cause.response?.status === 404
                            ? "not-found"
                            : "network"
                    );
                }
            } finally {
                if (active) setLoading(false);
            }
        }

        void load();
        return () => { active = false; };
    }, [drinkId, retry]);

    const location = pressedDrink?.location;
    const selectedSize =
        pressedDrink?.sizes.find(size => size.id === selectedSizeId)
        ?? pressedDrink?.sizes[0];
    const nutrition = selectedSize?.nutritionFacts ?? null;
    const hasCoordinates =
        location &&
        Number.isFinite(location.latitude) &&
        Number.isFinite(location.longitude);
    const distance =
        hasCoordinates && location ? distanceInMiles(CSUN, location) : null;

    const communityReviews = __DEV__ ? SAMPLE_REVIEWS : [];
    const reviews: Review[] = draftReview
        ? [
            {
                ...draftReview,
                id: "my-preview",
                name: "Your preview",
                date: "Just now",
                order: 5
            },
            ...communityReviews
        ]
        : communityReviews;

    const counts = [1, 2, 3, 4, 5].map(
        star => reviews.filter(item => item.rating === star).length
    );
    const average = reviews.length
        ? (
            reviews.reduce((total, item) => total + item.rating, 0)
            / reviews.length
        ).toFixed(1)
        : null;

    const sortedReviews = [...reviews].sort(
        (first, second) =>
            reviewSort === "highest"
                ? second.rating - first.rating || second.order - first.order
                : reviewSort === "lowest"
                    ? first.rating - second.rating || first.order - second.order
                    : second.order - first.order
    );

    async function openDirections() {
        if (!location || !hasCoordinates) return;

        const destination = `${location.latitude},${location.longitude}`;
        const url = Platform.OS === "ios"
            ? `http://maps.apple.com/?daddr=${destination}`
            : `https://www.google.com/maps/dir/?api=1&destination=${destination}`;

        try {
            await Linking.openURL(url);
        } catch {
            Alert.alert(
                "Directions unavailable",
                "Please try again later."
            );
        }
    }

    function addReview() {
        if (!rating) {
            Alert.alert(
                "Choose a star rating",
                "Tap a star before adding your review."
            );
            return;
        }
        if (!comment.trim()) {
            Alert.alert(
                "Write a review",
                "Add a comment before submitting."
            );
            return;
        }

        setDraftReview({ rating, comment: comment.trim() });
        setComment("");
        setReviewSort("recent");
        Alert.alert(
            "Preview only",
            "Your review is visible on this screen only. It was not posted to the café or saved online."
        );
    }

    return (
        <SafeAreaView style={styles.safeArea}>
            <ScrollView contentContainerStyle={styles.scrollContainer}>
                <Pressable
                    onPress={() => navigation.goBack()}
                    style={styles.backButton}
                >
                    <Ionicons name="arrow-back" size={22} color={theme.text} />
                </Pressable>

                {loading ? (
                    <ActivityIndicator
                        color={theme.icon}
                        accessibilityLabel="Loading drink details"
                    />
                ) : error || !pressedDrink ? (
                    <View style={styles.container}>
                        <Text
                            style={styles.errorText}
                            accessibilityRole="alert"
                        >
                            {error === "not-found"
                                ? "Drink not found."
                                : "Unable to load drink details."}
                        </Text>
                        {error === "network" && (
                            <Pressable
                                onPress={() => setRetry(value => value + 1)}
                            >
                                <Text style={styles.retryText}>Try again</Text>
                            </Pressable>
                        )}
                    </View>
                ) : (
                    <View style={styles.card}>
                        <View
                            style={styles.imagePlaceholder}
                            accessibilityLabel="Drink photo unavailable"
                        />

                        <View style={styles.titleRow}>
                            <Text style={styles.titleText}>
                                {pressedDrink.name}
                            </Text>
                            <Pressable
                                onPress={() => {
                                    setIsLiked(!isLiked);
                                    Alert.alert(
                                        "Preview only",
                                        "Favorites are not saved to your account yet."
                                    );
                                }}
                                style={styles.likeButton}
                                accessibilityRole="button"
                                accessibilityLabel={
                                    isLiked
                                        ? "Remove temporary favorite"
                                        : "Add temporary favorite"
                                }
                            >
                                <Ionicons
                                    name={isLiked ? "heart" : "heart-outline"}
                                    size={24}
                                    color={isLiked ? theme.icon : theme.text}
                                />
                            </Pressable>
                        </View>

                        <View style={styles.metaRow}>
                            <Text style={styles.locationText}>
                                {location?.name ?? "Café not listed"}
                                {distance !== null
                                    ? ` | ${distance.toFixed(1)} mi from CSUN`
                                    : ""}
                            </Text>
                            {selectedSize?.price != null && (
                                <Text style={styles.price}>
                                    ${selectedSize.price.toFixed(2)}
                                </Text>
                            )}
                        </View>

                        <View style={styles.starContainer}>
                            {[1, 2, 3, 4, 5].map(star => (
                                <Pressable
                                    key={star}
                                    onPress={() => setRating(star)}
                                    style={styles.starButton}
                                    accessibilityRole="button"
                                    accessibilityLabel={
                                        `Rate ${star} ${star === 1 ? "star" : "stars"}`
                                    }
                                >
                                    <Ionicons
                                        name={
                                            star <= rating
                                                ? "star"
                                                : "star-outline"
                                        }
                                        size={24}
                                        color="#C58B16"
                                    />
                                </Pressable>
                            ))}
                        </View>

                        <Text style={styles.nutritionTitle}>Size</Text>
                        {pressedDrink.sizes.length ? (
                            <View style={styles.optionsRow}>
                                {pressedDrink.sizes.map(size => (
                                    <Pressable
                                        key={size.id}
                                        onPress={() => setSelectedSizeId(size.id)}
                                        accessibilityRole="button"
                                        accessibilityLabel={`${size.sizeName} size`}
                                        style={[
                                            styles.option,
                                            selectedSize?.id === size.id &&
                                                styles.optionActive,
                                        ]}
                                    >
                                        <Text
                                            style={[
                                                styles.optionText,
                                                selectedSize?.id === size.id &&
                                                    styles.optionTextActive,
                                            ]}
                                        >
                                            {size.sizeName}
                                            {size.ounces != null
                                                ? ` · ${size.ounces} oz`
                                                : ""}
                                        </Text>
                                    </Pressable>
                                ))}
                            </View>
                        ) : (
                            <View style={styles.missingOption}>
                                <Text style={styles.missingOptionText}>
                                    Size information not provided.
                                </Text>
                            </View>
                        )}

                        <Text style={styles.nutritionTitle}>Milk</Text>
                        {pressedDrink.milkTypes?.length ? (
                            <View style={styles.optionsRow}>
                                {pressedDrink.milkTypes.map(milk => (
                                    <Pressable
                                        key={milk}
                                        onPress={() => setSelectedMilk(milk)}
                                        accessibilityRole="button"
                                        accessibilityLabel={`${milk} milk option`}
                                        style={[
                                            styles.option,
                                            selectedMilk === milk &&
                                                styles.optionActive,
                                        ]}
                                    >
                                        <Text
                                            style={[
                                                styles.optionText,
                                                selectedMilk === milk &&
                                                    styles.optionTextActive,
                                            ]}
                                        >
                                            {milk}
                                        </Text>
                                    </Pressable>
                                ))}
                            </View>
                        ) : (
                            <Text style={styles.ingredientsList}>
                                Milk options not provided.
                            </Text>
                        )}

                        <Text style={styles.nutritionTitle}>
                            Nutrition Facts
                        </Text>
                        <View style={styles.nutritionBox}>
                            <View
                                style={[
                                    styles.nutritionCell,
                                    styles.firstNutritionCell
                                ]}
                            >
                                <Text style={styles.nutritionLabel}>
                                    Calories
                                </Text>
                                <Text
                                    style={[
                                        styles.nutritionText,
                                        nutrition?.calories == null &&
                                            styles.nutritionMissingText
                                    ]}
                                >
                                    {nutrition?.calories != null
                                        ? `${nutrition.calories} cal`
                                        : "Not provided"}
                                </Text>
                            </View>

                            <View style={styles.nutritionCell}>
                                <Text style={styles.nutritionLabel}>
                                    Sugar
                                </Text>
                                <Text
                                    style={[
                                        styles.nutritionText,
                                        nutrition?.sugar == null &&
                                            styles.nutritionMissingText
                                    ]}
                                >
                                    {nutrition?.sugar != null
                                        ? `${nutrition.sugar} g`
                                        : "Not provided"}
                                </Text>
                            </View>

                            <View
                                style={[
                                    styles.nutritionCell,
                                    styles.lastNutritionCell
                                ]}
                            >
                                <Text style={styles.nutritionLabel}>
                                    Caffeine
                                </Text>
                                <Text
                                    style={[
                                        styles.nutritionText,
                                        nutrition?.caffeine == null &&
                                            styles.nutritionMissingText
                                    ]}
                                >
                                    {nutrition?.caffeine != null
                                        ? `${nutrition.caffeine} mg`
                                        : "Not provided"}
                                </Text>
                            </View>
                        </View>

                        <Pressable
                            onPress={() => setShowNutrition(!showNutrition)}
                            accessibilityRole="button"
                            style={styles.nutritionMore}
                        >
                            <Text style={styles.nutritionMoreText}>
                                Full nutrition facts
                            </Text>
                            <Ionicons
                                name={showNutrition ? "remove" : "add"}
                                size={23}
                                color={theme.icon}
                            />
                        </Pressable>

                        {showNutrition && (
                            <Text style={styles.nutritionNote}>
                                {nutrition
                                    ? [
                                        nutrition.totalFatG != null
                                            ? `Total fat: ${nutrition.totalFatG} g`
                                            : null,
                                        nutrition.cholesterolMg != null
                                            ? `Cholesterol: ${nutrition.cholesterolMg} mg`
                                            : null,
                                        nutrition.sodiumMg != null
                                            ? `Sodium: ${nutrition.sodiumMg} mg`
                                            : null,
                                        nutrition.carbsG != null
                                            ? `Carbs: ${nutrition.carbsG} g`
                                            : null,
                                        nutrition.proteinG != null
                                            ? `Protein: ${nutrition.proteinG} g`
                                            : null,
                                    ].filter(Boolean).join(" · ") ||
                                        "Additional nutrition facts not provided."
                                    : "Additional nutrition facts not provided."}
                            </Text>
                        )}

                        {selectedMilk && (
                            <Text style={styles.nutritionNote}>
                                Nutrition values are not adjusted for your milk
                                choice. Confirm with the café.
                            </Text>
                        )}

                        <Text style={styles.allergensTitle}>Allergens</Text>
                        <View style={styles.badgeRow}>
                            {pressedDrink.allergens?.length ? (
                                pressedDrink.allergens.map(allergen => (
                                    <View key={allergen} style={styles.badge}>
                                        <Ionicons
                                            name="warning-outline"
                                            size={14}
                                            color="#B45309"
                                        />
                                        <Text style={styles.allergenText}>
                                            {allergen}
                                        </Text>
                                    </View>
                                ))
                            ) : (
                                <View style={styles.allergenMissing}>
                                    <Text style={styles.allergenMissingText}>
                                        Allergen information not provided.
                                    </Text>
                                    <Text style={styles.allergenMissingText}>
                                        Confirm ingredients and cross-contact
                                        with the café before ordering.
                                    </Text>
                                </View>
                            )}
                        </View>

                        <Text style={styles.recipeTitle}>Ingredients</Text>
                        <Text style={styles.ingredientsList}>
                            Ingredients not provided.
                        </Text>

                        <Text style={styles.recipeTitle}>
                            Ratings &amp; Reviews
                        </Text>
                        {__DEV__ && (
                            <Text style={styles.reviewDisclaimer}>
                                Sample reviews for design only
                            </Text>
                        )}

                        {average && (
                            <View style={styles.reviewSummary}>
                                <View style={styles.reviewOverall}>
                                    <Text style={styles.averageRating}>
                                        {average}
                                    </Text>
                                    <Text style={styles.reviewStars}>
                                        {"★".repeat(
                                            Math.round(Number(average))
                                        )}
                                        {"☆".repeat(
                                            5 - Math.round(Number(average))
                                        )}
                                    </Text>
                                    <Text style={styles.reviewCount}>
                                        {reviews.length}{" "}
                                        {reviews.length === 1
                                            ? "rating"
                                            : "ratings"}
                                    </Text>
                                </View>

                                <View style={styles.ratingBars}>
                                    {[5, 4, 3, 2, 1].map(star => (
                                        <View
                                            key={star}
                                            style={styles.ratingBarRow}
                                        >
                                            <Text style={styles.ratingBarLabel}>
                                                {star}
                                            </Text>
                                            <View
                                                style={styles.ratingBarTrack}
                                            >
                                                <View
                                                    style={[
                                                        styles.ratingBarFill,
                                                        {
                                                            width: `${
                                                                reviews.length
                                                                    ? counts[star - 1] /
                                                                      reviews.length *
                                                                      100
                                                                    : 0
                                                            }%`
                                                        }
                                                    ]}
                                                />
                                            </View>
                                            <Text style={styles.ratingBarLabel}>
                                                {counts[star - 1]}
                                            </Text>
                                        </View>
                                    ))}
                                </View>
                            </View>
                        )}

                        <View>
                            <Text style={styles.reviewLabel}>
                                Leave a review
                            </Text>
                            <TextInput
                                style={styles.reviewInput}
                                multiline
                                maxLength={500}
                                value={comment}
                                onChangeText={setComment}
                                placeholder="What did you think of this drink?"
                                placeholderTextColor={theme.subtext}
                                accessibilityLabel="Review comment"
                            />
                            <Pressable
                                style={styles.reviewButton}
                                onPress={addReview}
                                accessibilityRole="button"
                            >
                                <Text style={styles.reviewButtonText}>
                                    Add review
                                </Text>
                            </Pressable>

                            {reviews.length ? (
                                <View style={styles.communitySection}>
                                    <View style={styles.reviewListHeader}>
                                        <Text style={styles.reviewListTitle}>
                                            {communityReviews.length
                                                ? "Community reviews"
                                                : "Your preview"}
                                        </Text>
                                    </View>

                                    {communityReviews.length > 0 && (
                                        <View style={styles.sortRow}>
                                            {(["recent", "highest", "lowest"] as const).map(
                                                option => (
                                                    <Pressable
                                                        key={option}
                                                        onPress={() => {
                                                            setReviewSort(option);
                                                            setVisibleReviews(2);
                                                        }}
                                                        style={[
                                                            styles.sortChip,
                                                            reviewSort === option &&
                                                                styles.sortChipActive
                                                        ]}
                                                        accessibilityRole="button"
                                                        accessibilityLabel={
                                                            `Sort by ${
                                                                option === "recent"
                                                                    ? "most recent"
                                                                    : option === "highest"
                                                                        ? "highest rated"
                                                                        : "lowest rated"
                                                            }`
                                                        }
                                                    >
                                                        <Text
                                                            style={[
                                                                styles.sortChipText,
                                                                reviewSort === option &&
                                                                    styles.sortChipTextActive
                                                            ]}
                                                        >
                                                            {option === "recent"
                                                                ? "Recent"
                                                                : option === "highest"
                                                                    ? "Highest"
                                                                    : "Lowest"}
                                                        </Text>
                                                    </Pressable>
                                                )
                                            )}
                                        </View>
                                    )}

                                    {sortedReviews
                                        .slice(0, visibleReviews)
                                        .map(item => (
                                            <View
                                                key={item.id}
                                                style={styles.reviewItem}
                                            >
                                                <View
                                                    style={styles.reviewByline}
                                                >
                                                    <View
                                                        style={styles.reviewAvatar}
                                                    >
                                                        <Text
                                                            style={
                                                                styles.reviewAvatarText
                                                            }
                                                        >
                                                            {item.name ===
                                                            "Your preview"
                                                                ? "You"
                                                                : item.name.slice(
                                                                    0, 1
                                                                )}
                                                        </Text>
                                                    </View>
                                                    <Text
                                                        style={
                                                            styles.reviewAuthor
                                                        }
                                                    >
                                                        {item.name}
                                                    </Text>
                                                    <Text
                                                        style={
                                                            styles.reviewDate
                                                        }
                                                    >
                                                        {item.date}
                                                    </Text>
                                                </View>
                                                <Text
                                                    style={styles.reviewRating}
                                                >
                                                    {"★".repeat(item.rating)}
                                                    {"☆".repeat(
                                                        5 - item.rating
                                                    )}
                                                </Text>
                                                <Text
                                                    style={styles.reviewComment}
                                                >
                                                    {item.comment}
                                                </Text>
                                            </View>
                                        ))}

                                    {visibleReviews <
                                        sortedReviews.length && (
                                        <Pressable
                                            style={styles.showMoreButton}
                                            onPress={() =>
                                                setVisibleReviews(
                                                    value => value + 2
                                                )
                                            }
                                            accessibilityRole="button"
                                            accessibilityLabel="Show more reviews"
                                        >
                                            <Text style={styles.showMoreText}>
                                                View{" "}
                                                {Math.min(
                                                    2,
                                                    sortedReviews.length -
                                                        visibleReviews
                                                )}{" "}
                                                more reviews
                                            </Text>
                                        </Pressable>
                                    )}
                                </View>
                            ) : (
                                <Text style={styles.reviewEmpty}>
                                    No community reviews available yet.
                                </Text>
                            )}
                        </View>

                        <View style={styles.buttonContainer}>
                            <Pressable
                                style={styles.getDirButton}
                                disabled={!hasCoordinates}
                                onPress={() => { void openDirections(); }}
                            >
                                <Text style={styles.getDirButtonText}>
                                    Get Directions
                                </Text>
                            </Pressable>
                            <Pressable
                                style={styles.viewStoreButton}
                                disabled={!location}
                                onPress={() =>
                                    navigation.navigate("CafeDetails", {
                                        locationId: location?.id
                                    })
                                }
                            >
                                <Text style={styles.viewStoreText}>
                                    View Store
                                </Text>
                            </Pressable>
                        </View>
                    </View>
                )}
            </ScrollView>
        </SafeAreaView>
    );
}

const makeStyles = (theme: Theme) =>
    StyleSheet.create({
        safeArea: {
            flex: 1,
            backgroundColor: theme.background
        },
        scrollContainer: {
            paddingHorizontal: 20,
            paddingBottom: 40
        },
        container: { paddingVertical: 24 },
        backButton: {
            width: 36,
            height: 36,
            borderRadius: 18,
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: theme.chip,
            marginTop: 8,
            marginBottom: 12
        },
        card: { paddingVertical: 12 },
        imagePlaceholder: {
            width: "100%",
            aspectRatio: 1,
            borderRadius: 8,
            backgroundColor: theme.band,
            marginBottom: 16
        },
        titleRow: {
            flexDirection: "row",
            justifyContent: "space-between",
            alignItems: "center",
            width: "100%"
        },
        likeButton: {
            width: 40,
            height: 40,
            borderRadius: 20,
            backgroundColor: theme.chip,
            alignItems: "center",
            justifyContent: "center"
        },
        titleText: {
            fontSize: 24,
            fontWeight: "bold",
            color: theme.text,
            flex: 1,
            marginRight: 12
        },
        errorText: {
            fontSize: 16,
            fontWeight: "bold",
            color: theme.text
        },
        locationText: {
            fontSize: 14,
            color: theme.subtext,
            flexShrink: 1
        },
        metaRow: {
            flexDirection: "row",
            justifyContent: "space-between",
            alignItems: "center",
            gap: 8
        },
        price: {
            fontWeight: "700",
            color: theme.text
        },
        starContainer: {
            flexDirection: "row",
            alignItems: "center",
            gap: 3,
            marginTop: 8,
            marginBottom: 12
        },
        starButton: { padding: 3 },
        nutritionTitle: {
            fontSize: 18,
            fontWeight: "bold",
            color: theme.text,
            marginTop: 22,
            marginBottom: 10
        },
        nutritionBox: {
            backgroundColor: theme.band,
            borderRadius: 16,
            minHeight: 94,
            paddingVertical: 19,
            paddingHorizontal: 14,
            flexDirection: "row",
            alignItems: "center",
            marginBottom: 10
        },
        nutritionCell: {
            flex: 1,
            minWidth: 0,
            paddingHorizontal: 8,
            borderRightWidth: StyleSheet.hairlineWidth,
            borderRightColor: theme.divider
        },
        firstNutritionCell: { paddingLeft: 0 },
        lastNutritionCell: {
            paddingRight: 0,
            borderRightWidth: 0
        },
        nutritionLabel: {
            fontSize: 13,
            color: theme.subtext,
            marginBottom: 6
        },
        nutritionMore: {
            flexDirection: "row",
            justifyContent: "space-between",
            alignItems: "center",
            borderTopWidth: StyleSheet.hairlineWidth,
            borderTopColor: theme.divider,
            minHeight: 50,
            paddingVertical: 12
        },
        nutritionMoreText: {
            fontSize: 16,
            fontWeight: "600",
            color: theme.icon
        },
        nutritionText: {
            fontSize: 20,
            fontWeight: "700",
            color: theme.text
        },
        nutritionMissingText: {
            fontSize: 12,
            fontWeight: "600"
        },
        nutritionNote: {
            fontSize: 12,
            color: theme.subtext,
            lineHeight: 18
        },
        allergensTitle: {
            fontSize: 16,
            fontWeight: "bold",
            color: theme.text,
            marginTop: 22,
            marginBottom: 10
        },
        badgeRow: {
            flexDirection: "row",
            flexWrap: "wrap",
            gap: 5
        },
        badge: {
            flexDirection: "row",
            alignItems: "center",
            backgroundColor: "#fde68a",
            paddingHorizontal: 10,
            paddingVertical: 6,
            borderRadius: 14,
            gap: 6
        },
        allergenText: {
            fontSize: 14,
            color: "#b45309",
            fontWeight: "600"
        },
        recipeTitle: {
            fontSize: 16,
            fontWeight: "bold",
            color: theme.text,
            marginTop: 22,
            marginBottom: 10
        },
        ingredientsList: {
            fontSize: 14,
            color: theme.subtext,
            lineHeight: 21,
            marginBottom: 8
        },
        missingOption: {
            backgroundColor: theme.chip,
            borderRadius: 14,
            padding: 12,
            alignSelf: "flex-start"
        },
        missingOptionText: {
            color: theme.subtext,
            fontSize: 14
        },
        optionsRow: {
            flexDirection: "row",
            flexWrap: "wrap",
            gap: 8
        },
        option: {
            backgroundColor: theme.chip,
            borderRadius: 18,
            paddingVertical: 9,
            paddingHorizontal: 16
        },
        optionActive: {
            backgroundColor: theme.chipActive
        },
        optionText: {
            color: theme.chipText,
            fontSize: 14,
            fontWeight: "600"
        },
        optionTextActive: {
            color: theme.chipActiveText
        },
        allergenMissing: {
            backgroundColor: theme.band,
            borderRadius: 14,
            padding: 14,
            gap: 5
        },
        allergenMissingText: {
            color: theme.subtext,
            fontSize: 13,
            lineHeight: 19
        },
        reviewLabel: {
            fontSize: 13,
            fontWeight: "600",
            color: theme.text,
            marginBottom: 8
        },
        reviewDisclaimer: {
            fontSize: 12,
            color: theme.subtext,
            marginBottom: 8
        },
        reviewSummary: {
            flexDirection: "row",
            alignItems: "center",
            gap: 18,
            paddingVertical: 16,
            borderBottomWidth: 1,
            borderBottomColor: theme.divider,
            marginBottom: 18
        },
        reviewOverall: {
            width: 95,
            alignItems: "center"
        },
        averageRating: {
            fontSize: 36,
            fontWeight: "700",
            color: theme.text
        },
        reviewStars: {
            fontSize: 15,
            color: "#A36D09"
        },
        reviewCount: {
            fontSize: 11,
            color: theme.subtext
        },
        ratingBars: {
            flex: 1,
            gap: 4
        },
        ratingBarRow: {
            flexDirection: "row",
            alignItems: "center",
            gap: 6
        },
        ratingBarLabel: {
            fontSize: 11,
            color: theme.subtext,
            width: 14
        },
        ratingBarTrack: {
            flex: 1,
            height: 6,
            borderRadius: 5,
            overflow: "hidden",
            backgroundColor: theme.chip
        },
        ratingBarFill: {
            height: "100%",
            backgroundColor: theme.icon,
            borderRadius: 5
        },
        reviewInput: {
            backgroundColor: theme.background,
            color: theme.text,
            borderRadius: 12,
            borderWidth: 1,
            borderColor: theme.inputBorder,
            minHeight: 92,
            padding: 12,
            textAlignVertical: "top"
        },
        reviewButton: {
            backgroundColor: theme.button,
            alignSelf: "flex-end",
            borderRadius: 24,
            paddingHorizontal: 16,
            paddingVertical: 10,
            marginTop: 10
        },
        reviewButtonText: {
            color: theme.buttonText,
            fontWeight: "700",
            fontSize: 13
        },
        communitySection: {
            marginTop: 22,
            borderTopWidth: 1,
            borderTopColor: theme.divider,
            paddingTop: 18
        },
        reviewListHeader: {
            flexDirection: "row",
            alignItems: "center",
            justifyContent: "space-between"
        },
        reviewListTitle: {
            fontSize: 15,
            color: theme.text,
            fontWeight: "700"
        },
        sortRow: {
            flexDirection: "row",
            gap: 7,
            marginTop: 12
        },
        sortChip: {
            borderRadius: 16,
            borderWidth: 1,
            borderColor: theme.inputBorder,
            paddingHorizontal: 10,
            paddingVertical: 6
        },
        sortChipActive: {
            backgroundColor: theme.chipActive,
            borderColor: theme.chipActive
        },
        sortChipText: {
            fontSize: 12,
            color: theme.text
        },
        sortChipTextActive: {
            color: theme.chipActiveText
        },
        reviewItem: {
            paddingVertical: 15,
            borderBottomWidth: 1,
            borderBottomColor: theme.divider
        },
        reviewByline: {
            flexDirection: "row",
            alignItems: "center",
            gap: 8
        },
        reviewAvatar: {
            width: 34,
            height: 34,
            borderRadius: 17,
            backgroundColor: theme.chip,
            alignItems: "center",
            justifyContent: "center"
        },
        reviewAvatarText: {
            fontSize: 11,
            fontWeight: "700",
            color: theme.chipText
        },
        reviewAuthor: {
            fontSize: 13,
            fontWeight: "700",
            color: theme.text,
            flex: 1
        },
        reviewDate: {
            fontSize: 11,
            color: theme.subtext
        },
        reviewRating: {
            color: "#A36D09",
            fontSize: 14,
            fontWeight: "700"
        },
        reviewComment: {
            color: theme.text,
            fontSize: 14,
            lineHeight: 21,
            marginTop: 5
        },
        showMoreButton: {
            alignSelf: "center",
            justifyContent: "center",
            minHeight: 44,
            paddingHorizontal: 12,
            marginTop: 8
        },
        showMoreText: {
            fontSize: 13,
            fontWeight: "700",
            color: theme.icon,
            textDecorationLine: "underline"
        },
        reviewEmpty: {
            fontSize: 13,
            color: theme.subtext,
            marginTop: 16
        },
        retryText: {
            color: theme.button,
            fontSize: 16,
            fontWeight: "700",
            paddingTop: 12
        },
        buttonContainer: {
            marginTop: 26,
            gap: 10
        },
        getDirButtonText: {
            fontSize: 16,
            fontWeight: "bold",
            color: theme.buttonText
        },
        viewStoreText: {
            fontSize: 16,
            fontWeight: "bold",
            color: theme.text
        },
        getDirButton: {
            paddingVertical: 14,
            borderRadius: 28,
            backgroundColor: theme.button,
            alignItems: "center",
            justifyContent: "center",
            marginVertical: 0
        },
        viewStoreButton: {
            paddingVertical: 14,
            borderRadius: 28,
            alignItems: "center",
            justifyContent: "center",
            borderWidth: 1,
            borderColor: theme.inputBorder,
            marginVertical: 0
        }
    });
