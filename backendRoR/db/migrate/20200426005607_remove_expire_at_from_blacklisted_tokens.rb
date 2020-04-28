class RemoveExpireAtFromBlacklistedTokens < ActiveRecord::Migration[6.0]
  def change

    remove_column :blacklisted_tokens, :expire_at, :datetime
  end
end
