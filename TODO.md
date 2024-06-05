# PPU
 - [ ] Investigate glitch in mario 1 when mario overlaps score counter
 - [ ] Clean up code in PPU2C02
 - [ ] Investigate / implement sequencer based implementation (uses more memory but less CPU)
 - [ ] Implement missing control flags
 - [ ] Implement sprite overflow
 - [ ] Implement hiding of background / sprites on left side of screen

# CPU
 - [ ] Replace switches with sequencers?
 - [ ] Remove creation of new exception on dma halt (prevent allocating new objects so gc does not interfere with game loop)

# Display
 - [ ] Use non-static key bindings and support second controller
 - [ ] Support frame resizing and add toolbar
 - [ ] Add basic runtime-toggleable debug utilities such as FPS counter and raster overlay
 - [ ] Add support for choosing custom palette instead of default.pal
 - [ ] Add pause/resume emulation feature
 - [ ] Add emulation speed controls

# Mapper
 - [ ] Implement more advanced mappers to support more games

# Sound
 - [ ] Fix occasional cracking and seemingly slightly too high pitch / clock speed

# APU
 - [ ] Add triangle generator
 - [ ] Add noise generator
 - [ ] Trigger NMIs
 - [ ] Add DMC

# General
 - [ ] Improve dependency management (using DI?)
 - [ ] Split over multiple JARs to add support for new frontends (android / web)?
 - [ ] Investigate rewriting core components natively
 - [ ] Clean up test code