---
name: RoutePlanningNavigationSkill
description: This skill is used by an Android Agent to plan a route and open a navigation session in a map application.
version: 1.0.0
---
# Route Navigation Planning Agent Skill

## Skill Name

Route Navigation Planning

## Description

This skill is used by an Android agent to plan and start route navigation based on the user’s destination, preferred map application, current phone location, installed map applications, last used map application, and travel mode.

The agent should determine the map application name and package name, starting point, destination, and travel mode, then invoke the corresponding map navigation tool and monitor the screen until the target map application enters navigation mode.

## Goal

Start route navigation for the user using an appropriate map application and travel mode.

## Required Tools

- Installed application list tool
- Phone location tool
- Map navigation tool

## Workflow

### 1. Determine the Map Application

The selected map application must include both the application display name and the package name. The package name is required when invoking the map navigation tool because deep links may fail or open the wrong target without it.

1. Get the list of currently installed map applications on the phone, including each application’s display name and package name.
2. If no map application is installed, notify the user:

   `No map application is currently available. Task terminated.`

3. Select the map application using the following priority order:
    - The map application explicitly specified in the user’s current input.
    - The map application extracted from user preferences.
    - The last used map application.
    - A randomly selected installed map application.
4. If a selected application is not installed or its package name cannot be resolved, skip it and continue to the next priority.
5. If randomly selecting a map application, choose only from installed map applications with a known package name.
6. The final selected map application must be represented as:
    - `map_app_name`
    - `map_app_package_name`

### 2. Determine the Starting Point

1. Use the phone’s current location to get the latitude and longitude.
2. If location retrieval fails, notify the user:

   `Failed to get location.`

### 3. Determine the Destination

1. Check whether the user has previously navigated to the target destination.
2. If the destination exists in navigation history, directly complete the full destination information.
3. If no matching destination exists, determine whether the user’s input contains enough information to identify the destination.
4. If the destination cannot be determined with enough confidence, ask the user to provide more information.

### 4. Determine the Travel Mode

1. If the user has not specified a travel mode, check whether a **user travel mode preference** exists.
2. If no user travel mode preference exists, get the phone’s current location.
3. If location retrieval fails, notify the user:

   `Failed to get location.`

4. If location retrieval succeeds, determine the best travel mode based on the relationship between the current location and the destination.

Supported travel modes may include:

- Driving
- Walking
- Cycling
- Electric bicycle
- Public transportation

### 5. Start Map Navigation

1. After determining the **map application name**, **map application package name**, **destination**, and **travel mode**, call the corresponding map navigation tool.
2. The map navigation tool call must include `map_app_package_name`. Do not invoke the tool with only the application display name.

## Completion Criteria

The task is complete when the selected map application successfully enters the corresponding navigation mode.

## Failure Conditions

The skill should terminate and notify the user when any of the following occurs:

1. No map application is installed.
2. The phone location cannot be retrieved.
3. The destination cannot be determined and the user does not provide enough additional information.
4. The selected map application package name cannot be resolved.
5. The map navigation tool fails to open navigation.

## Agent Behavior Requirements

- Do not ask the user for missing information unless the destination cannot be determined.
- Select the map application in this order: user’s current input, extracted user preference, last used map application, random installed map application.
- Always resolve and pass the selected map application package name to the map navigation tool.
- Prefer navigation history when it can complete the destination.
- Use current phone location as the starting point.
- Select the map application automatically when the user does not specify one.
- Select the travel mode automatically when the user does not specify one.
- Automatically handle location permission dialogs and common blocking pop-ups.
