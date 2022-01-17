ALTER TABLE rsvp
    ADD COLUMN waitlist LONGTEXT not null default ''
        after UNDECIDED;
