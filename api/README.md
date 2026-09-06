# Robot Wars API

Interfaces and base classes for the Robot Wars battle engine. This module defines the contracts that all robots and game components must follow.

## Contents

This module contains:

- Robot interface: Base contract for all robot implementations
- Droid interface: Contract for droid robots without radar
- TeamLeader interface: Contract for team leader robots with radar
- Battlefield interface: Contract for the game battlefield
- BattlefieldEngine: Core game engine implementation
- Additional interfaces and classes for robot logic, view components, and factories

## Usage

Other modules depend on this module. Robot implementations must implement the Robot, Droid, or TeamLeader interfaces to participate in battles.
